package pl.edu.agh;

import java.util.LinkedList;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class LocationLoggingService extends Service {

	private static final int MAX_HISTORY_SIZE = 1000;
	private static final int NOTIF_ID = 435;
	
	private final IBinder binder = new ServiceAccess();
	
	private Handler handler;
	private boolean logging = false;
	
	private NotificationManager notificationManager;
	
	private LinkedList<Location> locationHistory = new LinkedList<Location>();
	private LocationManager locationManager;
	private LocationListener locationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
		@Override
		public void onProviderEnabled(String provider) {
		}
		
		@Override
		public void onProviderDisabled(String provider) {
		}
		
		@Override
		public void onLocationChanged(Location location) {
			if(locationHistory.size() == MAX_HISTORY_SIZE) {
				locationHistory.removeLast();
			}
			locationHistory.addFirst(location);
			sendLocationUpdateMessage();
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			createGpsDisabledAlert();
		}
		startLogging();
		return START_STICKY;
	}
	
	public class ServiceAccess extends Binder {
		
		public LocationLoggingService getService() {
			return LocationLoggingService.this;
		}
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		
		int icon = R.drawable.globe;
		CharSequence tickerText = getString(R.string.gps_notif_ticker);
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.FLAG_NO_CLEAR;
		
		Context context = getApplicationContext();
		CharSequence contentTitle = getString(R.string.gps_notif_title);
		CharSequence contentText = getString(R.string.gps_notif_text);
		
		Intent notificationIntent = new Intent(this, HomeActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIF_ID,  notification);
	}
	
	@Override
	public void onDestroy() {
		stopLogging();
		
		notificationManager.cancel(NOTIF_ID);
	}
	
	public void registerHandler(Handler handler) {
		this.handler = handler;
	}
	
	public void unregisterHander(Handler hander) {
		this.handler = null;
	}
	
	public boolean isLogging() {
		return logging;
	}
	
	public void startLogging() {
		if(!logging) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
			logging = true;
		}
	}
	
	public void stopLogging() {
		
		if(logging) {
			logging = false;
			locationManager.removeUpdates(locationListener);
		}
	}
	
	public void updateCurrentPosition() {
		
		sendLocationUpdateMessage();
		
	}
	
	private void sendLocationUpdateMessage() {

		if(handler == null || locationHistory.isEmpty()) {
			return;
		}
		
		Location location = locationHistory.getFirst();
		
		double lon = location.getLongitude();
		double lat = location.getLatitude();
		float speed = location.getSpeed();
		float course = location.getBearing();
		float acc = location.getAccuracy();
	
		Message msg = handler.obtainMessage();
		Bundle data = new Bundle();
		data.putDouble("lon", lon);
		data.putDouble("lat", lat);
		data.putFloat("speed", speed);
		data.putFloat("course", course);
		data.putFloat("accuracy", acc);
		msg.setData(data);
		
		handler.handleMessage(msg);
	}
	
	private void createGpsDisabledAlert(){  
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	builder.setMessage("Your GPS is disabled! Would you like to enable it?")  
    	     .setCancelable(false)  
    	     .setPositiveButton("Enable GPS",  
    	          new DialogInterface.OnClickListener(){  
    	          public void onClick(DialogInterface dialog, int id){

    	        	  Intent gpsOptionsIntent = new Intent(  
    	                      android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
    	              startActivity(gpsOptionsIntent);
    	          }  
    	     });  
    	     builder.setNegativeButton("Do nothing",  
    	          new DialogInterface.OnClickListener(){  
    	          public void onClick(DialogInterface dialog, int id){  
    	               dialog.cancel();  
    	          }  
    	     });  
    	AlertDialog alert = builder.create();  
    	alert.show();  
	}  
	
}