package pl.edu.agh.android.components;

import pl.edu.agh.android.components.R;
import pl.edu.agh.android.components.LocationLoggingService.ServiceAccess;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class HomeActivity extends Activity {

	public static final int UPDATE_LOCATION_MSG = 1;
	public static final int ASK_GPS_ACTIVATE_MSG = 2;
	
	private Handler guiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			int whatMessage = msg.what;
			switch(whatMessage) {
			case UPDATE_LOCATION_MSG:
				Bundle data = msg.getData();
				double lon = data.getDouble("lon");
				double lat = data.getDouble("lat");
				float speed = data.getFloat("speed");
				float course = data.getFloat("course");
				float accur = data.getFloat("accuracy");

				TextView longView = (TextView)findViewById(R.id.long_val);
				TextView lattView = (TextView)findViewById(R.id.latt_val);
				TextView speedView = (TextView)findViewById(R.id.speed_val);
				TextView courseView = (TextView)findViewById(R.id.course_val);
				TextView accView = (TextView)findViewById(R.id.acc_val);
				
				longView.setText(Double.toString(lon));
				lattView.setText(Double.toString(lat));
				speedView.setText(Float.toString(speed));
				courseView.setText(Float.toString(course));
				accView.setText(Float.toString(accur));
				break;
			case ASK_GPS_ACTIVATE_MSG:
				final ToggleButton startStopButton = (ToggleButton)findViewById(R.id.start_stop_button);
				startStopButton.setChecked(false);
				createGpsDisabledAlert();
				break;
				
			}
		}
	};
	
	private boolean bound = false;
	private LocationLoggingService loggingService;
	private ServiceConnection loggingServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			
			bound = true; 
			loggingService = ((ServiceAccess)service).getService();
			loggingService.registerHandler(guiHandler);
			loggingService.updateCurrentPosition();

			final ToggleButton startStopButton = (ToggleButton)findViewById(R.id.start_stop_button);
			if(loggingService.isLogging()) {
				startStopButton.setChecked(true);
				
			} else {
				startStopButton.setChecked(false);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = false;
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final ToggleButton startStopButton = (ToggleButton)findViewById(R.id.start_stop_button);
        startStopButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
        	@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        		if(bound) {
	        		if(isChecked) {
	        			startService(new Intent(HomeActivity.this, LocationLoggingService.class));
	        		} else {
	        			
	        			loggingService.stopLogging();
	        			stopService(new Intent(HomeActivity.this, LocationLoggingService.class));
	        		}
        		}
			}
		});
        
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	if(!bound) {
    		this.bindService(new Intent(this, LocationLoggingService.class),
    				loggingServiceConnection, Context.BIND_AUTO_CREATE);
    	}
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	if(bound) {
    		loggingService.unregisterHander(guiHandler);
    		bound = false;
    		this.unbindService(loggingServiceConnection);
    	}
    }
    
	private void createGpsDisabledAlert() {  
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