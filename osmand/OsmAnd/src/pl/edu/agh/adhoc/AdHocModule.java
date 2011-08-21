package pl.edu.agh.adhoc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Hashtable;

import net.osmand.plus.R;
import net.osmand.plus.activities.MainMenuActivity;
import pl.edu.agh.adhoc.system.Configuration;
import pl.edu.agh.adhoc.system.CoreTask;
import pl.edu.agh.logic.TrafficDataProvider;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class AdHocModule {

	public Application application;
	public static final String MSG_TAG = "AdHoc Wifi";
	public static final int DEFAULT_LISTEN_PORT = 2340;
	
	public final String DEFAULT_PASSPHRASE = "abcdefghijklm";
	public final String DEFAULT_LANNETWORK = "192.168.2.0/24";
	public final String DEFAULT_ENCSETUP   = "wpa_supplicant";
	
	// StartUp-Check perfomed
	public boolean startupCheckPerformed = false;
	
	// WifiManager
	private WifiManager wifiManager;
	
	// Preferences
	public SharedPreferences settings = null;
	public SharedPreferences.Editor preferenceEditor = null;
	
    // Notification
	public NotificationManager notificationManager;
	private Notification notification;
	
	// Intents
	private PendingIntent mainIntent;
	    
	// Original States
	private static boolean origWifiState = false;
		
	// Supplicant
	public CoreTask.WpaSupplicant wpasupplicant = null;
	// TiWlan.conf
	public CoreTask.TiWlanConf tiwlan = null;
	// tether.conf
	public CoreTask.TetherConfig tethercfg = null;
	
	// CoreTask
	public CoreTask coretask = null;
	public AdHocBroadcastSocket socket = null;
	public TrafficDataProvider trafficDataProvider = null;
	
	public AdHocModule(Application application) {
		this.application = application;
		
	}
	
	public void setTrafficDataProvider(TrafficDataProvider trafficDataProvider) {
		this.trafficDataProvider = trafficDataProvider;
	}
	
	public void init() {
		Log.d(MSG_TAG, "Calling init()");
		
		//create CoreTask
		this.coretask = new CoreTask();
		this.coretask.setPath(application.getApplicationContext().getFilesDir().getParent());
		Log.d(MSG_TAG, "Current directory is "+application.getApplicationContext().getFilesDir().getParent());

	    // Check Homedir, or create it
        this.checkDirs(); 
        
        // Preferences
		this.settings = PreferenceManager.getDefaultSharedPreferences(application);
		
        // preferenceEditor
        this.preferenceEditor = settings.edit();
		
        // init wifiManager
        wifiManager = (WifiManager) application.getSystemService(Context.WIFI_SERVICE); 
        
        // Supplicant config
        this.wpasupplicant = this.coretask.new WpaSupplicant();
        
        // tiwlan.conf
        this.tiwlan = this.coretask.new TiWlanConf();
        
        // tether.cfg
        this.tethercfg = this.coretask.new TetherConfig();
        this.tethercfg.read();

        this.notificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
    	this.notification = new Notification(R.drawable.start_notification, "AdHoc Wifi", System.currentTimeMillis());
    	this.mainIntent = PendingIntent.getActivity(application, 0, new Intent(application, MainMenuActivity.class), 0);

		this.socket = new AdHocBroadcastSocket();
	}

	public void finish() {
		Log.d(MSG_TAG, "Calling finish()");

		this.stopAdHoc();
		this.notificationManager.cancelAll();
	
	}
	
	public void updateConfiguration() {
		
		long startStamp = System.currentTimeMillis();
		
		String deviceType = Configuration.getDeviceType();
		
        boolean bluetoothPref = this.settings.getBoolean("bluetoothon", false);
		boolean wepEnabled = this.settings.getBoolean("encpref", false);
		String ssid = this.settings.getString("ssidpref", "AndroidTether");
        String txpower = this.settings.getString("txpowerpref", "disabled");
        String lannetwork = this.settings.getString("lannetworkpref", DEFAULT_LANNETWORK);
        String wepkey = this.settings.getString("passphrasepref", DEFAULT_PASSPHRASE);
        String wepsetupMethod = this.settings.getString("encsetuppref", DEFAULT_ENCSETUP);
        String channel = this.settings.getString("channelpref", "1");
        
		// tether.conf
        String subnet = lannetwork.substring(0, lannetwork.lastIndexOf("."));
        this.tethercfg.read();
		this.tethercfg.put("device.type", deviceType);
        this.tethercfg.put("tether.mode", bluetoothPref ? "bt" : "wifi");
        this.tethercfg.put("wifi.essid", ssid);
        this.tethercfg.put("wifi.channel", channel);
		this.tethercfg.put("ip.network", lannetwork.split("/")[0]);
		this.tethercfg.put("ip.gateway", subnet + ".254");        
		this.tethercfg.put("wifi.interface", this.coretask.getProp("wifi.interface"));
		this.tethercfg.put("wifi.txpower", txpower);

		// wepEncryption
		if (wepEnabled) {
			this.tethercfg.put("wifi.encryption", "wep");
			this.tethercfg.put("wifi.wepkey", wepkey);
			// Getting encryption-method if setup-method on auto 
			if (wepsetupMethod.equals("auto")) {
				wepsetupMethod = Configuration.getEncryptionAutoMethod(deviceType);
			}
			// Setting setup-mode
			this.tethercfg.put("wifi.setup", wepsetupMethod);
			// Prepare wpa_supplicant-config if wpa_supplicant selected
			if (wepsetupMethod.equals("wpa_supplicant")) {
				// Install wpa_supplicant.conf-template
				if (this.wpasupplicant.exists() == false) {
					this.installWpaSupplicantConfig();
				}
				
				// Update wpa_supplicant.conf
				Hashtable<String,String> values = new Hashtable<String,String>();
				values.put("ssid", "\""+this.settings.getString("ssidpref", "TrafficAdHoc")+"\"");
				values.put("wep_key0", "\""+this.settings.getString("passphrasepref", DEFAULT_PASSPHRASE)+"\"");
				this.wpasupplicant.write(values);
			}
        }
		else {
			this.tethercfg.put("wifi.encryption", "disabled");
			this.tethercfg.put("wifi.wepkey", "");
			
			// Make sure to remove wpa_supplicant.conf
			if (this.wpasupplicant.exists()) {
				this.wpasupplicant.remove();
			}			
		}
		
		// determine driver wpa_supplicant
		this.tethercfg.put("wifi.driver", Configuration.getWpaSupplicantDriver(deviceType));
		
		// writing config-file
		if (this.tethercfg.write() == false) {
			Log.e(MSG_TAG, "Unable to update tether.conf!");
		}
				
		/*
		 * TODO
		 * Need to find a better method to identify if the used device is a
		 * HTC Dream aka T-Mobile G1
		 */
		if (deviceType.equals(Configuration.DEVICE_DREAM)) {
			Hashtable<String,String> values = new Hashtable<String,String>();
			values.put("dot11DesiredSSID", this.settings.getString("ssidpref", "TrafficAdHoc"));
			values.put("dot11DesiredChannel", this.settings.getString("channelpref", "1"));
			this.tiwlan.write(values);
		}
		
		Log.d(MSG_TAG, "Creation of configuration-files took ==> "+(System.currentTimeMillis()-startStamp)+" milliseconds.");
	}
	
	// Start/Stop AdHoc
    public boolean startAdHoc() {
        // Updating all configs
        this.updateConfiguration();

        this.disableWifi();
        
    	// Starting service
    	if (this.coretask.runRootCommand(this.coretask.DATA_FILE_PATH+"/bin/tether start 1")) {

    		AdHocModule.this.copyFile(AdHocModule.this.coretask.DATA_FILE_PATH+"/conf/ad_hoc.pid", "550", R.raw.ad_hoc_pid);
			String listenAddress = this.tethercfg.get("ip.gateway");
	    	InetAddress address;
			try {
			
				address = InetAddress.getByName(listenAddress);
				this.socket.open(address, DEFAULT_LISTEN_PORT);

			} catch (UnknownHostException e) {
				Log.e(MSG_TAG, "Wrong ad-hoc listen address", e);

			} catch (SocketException e) {
				Log.e(MSG_TAG, "ad-hoc broad cast socket error", e);
			}
			if(trafficDataProvider != null) {
				trafficDataProvider.startAdHocServer();
			}
			return true;
    		
    	}
    	return false;
    }
    
    public boolean stopAdHoc() {

    	boolean stopped = this.coretask.runRootCommand(this.coretask.DATA_FILE_PATH+"/bin/tether stop 1");
		this.notificationManager.cancelAll();
		AdHocModule.this.removeFile(AdHocModule.this.coretask.DATA_FILE_PATH+"/conf/ad_hoc.pid");
		
		this.socket.close();
		if(trafficDataProvider != null) {
			trafficDataProvider.stopAdHocServer();
		}
		this.enableWifi();
		return stopped;
    }
	
    public boolean restartAdHoc() {
    	boolean status = this.coretask.runRootCommand(this.coretask.DATA_FILE_PATH+"/bin/tether stop 1");
		this.notificationManager.cancelAll();

		// Updating all configs
        this.updateConfiguration();       
        
        this.disableWifi();
        // Starting service
        if (status == true)
        	status = this.coretask.runRootCommand(this.coretask.DATA_FILE_PATH+"/bin/tether start 1");
        
        this.showStartNotification();
        return status;
    }
    
    public String getNetworkDevice() {
    	
    	return this.coretask.getProp("wifi.interface");
    }
    
    // gets user preference on whether wakelock should be disabled during tethering
    public boolean isWakeLockDisabled(){
		return this.settings.getBoolean("wakelockpref", true);
	} 
	
    // Wifi
    public void disableWifi() {
    	if (this.wifiManager.isWifiEnabled()) {
    		origWifiState = true;
    		this.wifiManager.setWifiEnabled(false);
    		Log.d(MSG_TAG, "Wifi disabled!");
        	// Waiting for interface-shutdown
    		try {
    			Thread.sleep(5000);
    		} catch (InterruptedException e) {
    			// nothing
    		}
    	}
    }
    
    public void enableWifi() {
    	if (origWifiState) {
        	// Waiting for interface-restart
    		this.wifiManager.setWifiEnabled(true);
    		try {
    			Thread.sleep(5000);
    		} catch (InterruptedException e) {
    			// nothing
    		}
    		Log.d(MSG_TAG, "Wifi started!");
    	}
    }
    
  
    public int getNotificationType() {
		return Integer.parseInt(this.settings.getString("notificationpref", "2"));
    }
    
    // Notification
    public void showStartNotification() {
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.setLatestEventInfo(application, "AdHoc Wifi", "AdHoc is currently running ...", this.mainIntent);
    	this.notificationManager.notify(-1, this.notification);
    }
    
    public boolean binariesExists() {
    	File file = new File(this.coretask.DATA_FILE_PATH+"/bin/tether");
    	return file.exists();
    }
    
    public boolean isProcessRunning() {
    	File pidFile = new File(this.coretask.DATA_FILE_PATH+"/conf/ad_hoc.pid");
    	return pidFile.exists();
    }
    
    public void installWpaSupplicantConfig() {
    	this.copyFile(this.coretask.DATA_FILE_PATH+"/conf/wpa_supplicant.conf", "0644", R.raw.wpa_supplicant_conf);
    }
    
    Handler displayMessageHandler = new Handler(){
        public void handleMessage(Message msg) {
       		if (msg.obj != null) {
       			AdHocModule.this.displayToastMessage((String)msg.obj);
       		}
        	super.handleMessage(msg);
        }
    };
 
    public void renewLibrary() {
    	File libNativeTaskFile = new File(AdHocModule.this.coretask.DATA_FILE_PATH+"/library/libnativetask.so");
    	if (libNativeTaskFile.exists()){
    		libNativeTaskFile.renameTo(new File(AdHocModule.this.coretask.DATA_FILE_PATH+"/library/libnativetask.so"));
    	}
    }    
    
    public void installFiles() {
    	new Thread(new Runnable(){
			public void run(){
				String message = null;
				// libnativeTask.so	
				if (message == null) {
					File libNativeTaskFile = new File(AdHocModule.this.coretask.DATA_FILE_PATH+"/library/libnativetask.so");
					if (libNativeTaskFile.exists()) {
						message = AdHocModule.this.copyFile(AdHocModule.this.coretask.DATA_FILE_PATH+"/library/libnativetask.so", R.raw.libnativetask_so);
					}
					else {
						message = AdHocModule.this.copyFile(AdHocModule.this.coretask.DATA_FILE_PATH+"/library/libnativetask.so", R.raw.libnativetask_so);
					}
				}
				// tether
		    	if (message == null) {
			    	message = AdHocModule.this.copyFile(AdHocModule.this.coretask.DATA_FILE_PATH+"/bin/tether", "0755", R.raw.tether);
		    	}
		    	// ifconfig
		    	if (message == null) {
			    	message = AdHocModule.this.copyFile(AdHocModule.this.coretask.DATA_FILE_PATH+"/bin/ifconfig", "0755", R.raw.ifconfig);
		    	}	
		    	// iwconfig
		    	if (message == null) {
			    	message = AdHocModule.this.copyFile(AdHocModule.this.coretask.DATA_FILE_PATH+"/bin/iwconfig", "0755", R.raw.iwconfig);
		    	}
				
		    	// tiwlan.ini
				if (message == null) {
					AdHocModule.this.copyFile(AdHocModule.this.coretask.DATA_FILE_PATH+"/conf/tiwlan.ini", "0644", R.raw.tiwlan_ini);
				}
				// edify script
				if (message == null) {
					AdHocModule.this.copyFile(AdHocModule.this.coretask.DATA_FILE_PATH+"/conf/tether.edify", "0644", R.raw.tether_edify);
				}
				// tether.cfg
				if (message == null) {
					AdHocModule.this.copyFile(AdHocModule.this.coretask.DATA_FILE_PATH+"/conf/tether.conf", "0644", R.raw.tether_conf);
				}
				
				// wpa_supplicant drops privileges, we need to make files readable.
				AdHocModule.this.coretask.chmod(AdHocModule.this.coretask.DATA_FILE_PATH+"/conf/", "0755");

				if (message == null) {
			    	message = "Binaries and config-files installed!";
				}
				
				// Sending message
				Message msg = new Message();
				msg.obj = message;
				AdHocModule.this.displayMessageHandler.sendMessage(msg);
			}
		}).start();
    }
   
    private String removeFile(String filename) {
    	
    	File outFile = new File(filename);
    	Log.d(MSG_TAG, "Deleting file '"+filename+"' ...");
    	
    	if(!outFile.delete()) {
    		return "Couldn't delete file - "+filename+"!";
    	}
		return null;
    }
    
    private String copyFile(String filename, String permission, int ressource) {
    	String result = this.copyFile(filename, ressource);
    	if (result != null) {
    		return result;
    	}
    	if (this.coretask.chmod(filename, permission) != true) {
    		result = "Can't change file-permission for '"+filename+"'!";
    	}
    	return result;
    }
    
    private String copyFile(String filename, int ressource) {
    	File outFile = new File(filename);
    	Log.d(MSG_TAG, "Copying file '"+filename+"' ...");
    	InputStream is = this.application.getResources().openRawResource(ressource);
    	byte buf[] = new byte[1024];
        int len;
        try {
        	OutputStream out = new FileOutputStream(outFile);
        	while((len = is.read(buf))>0) {
				out.write(buf,0,len);
			}
        	out.close();
        	is.close();
		} catch (IOException e) {
			return "Couldn't install file - "+filename+"!";
		}
		return null;
    }
    
    private void checkDirs() {
    	File dir = new File(this.coretask.DATA_FILE_PATH);
    	if (dir.exists() == false) {
    			this.displayToastMessage("Application data-dir does not exist!");
    	}
    	else {
    		String[] dirs = { "/bin", "/var", "/conf", "/library"};
    		for (String dirname : dirs) {
    			dir = new File(this.coretask.DATA_FILE_PATH + dirname);
    	    	if (dir.exists() == false) {
    	    		if (!dir.mkdir()) {
    	    			this.displayToastMessage("Couldn't create " + dirname + " directory!");
    	    		}
    	    	}
    	    	else {
    	    		Log.d(MSG_TAG, "Directory '"+dir.getAbsolutePath()+"' already exists!");
    	    	}
    		}
    	}
    }
    
   
    // Display Toast-Message
	public void displayToastMessage(String message) {
		Toast.makeText(application.getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	
}
