package pl.edu.agh;

import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

//TODO use background Service
public class HomeActivity extends Activity {

	private int HISTORY_SIZE = 1000;
	
	private Handler guiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
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
		}
	};
	
	//TODO: save history to shared prefs
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
			if(locationHistory.size() == HISTORY_SIZE) {
				locationHistory.removeLast();
			}
			locationHistory.addFirst(location);
			
			double lon = location.getLongitude();
			double lat = location.getLatitude();
			float speed = location.getSpeed();
			float course = location.getBearing();
			float acc = location.getAccuracy();
		
			Message msg = guiHandler.obtainMessage();
			Bundle data = new Bundle();
			data.putDouble("lon", lon);
			data.putDouble("lat", lat);
			data.putFloat("speed", speed);
			data.putFloat("course", course);
			data.putFloat("accuracy", acc);
			msg.setData(data);
			
			guiHandler.handleMessage(msg);
			
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

        		if(isChecked) {
        			if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        				createGpsDisabledAlert();
		    		
        			}
        			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        					2000, 0, locationListener);
        		} else {
        		
        			locationManager.removeUpdates(locationListener);
        		}
			}
		});
	
        
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	locationManager.removeUpdates(locationListener);
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