package pl.edu.agh.service;

import java.util.LinkedList;
import java.util.List;

import pl.edu.agh.logic.LocationDataSender;
import pl.edu.agh.logic.PeriodicSendingStrategy;
import pl.edu.agh.logic.SendingStrategy;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class LocationLogger extends Service implements LocationDataSource {

	private static final int GPS_UPDATE_INTERVAL = 1000;
	private static final int LOCATION_HISTORY_MAX_SIZE = 1000;
	private static final long LOCATION_SENDING_INTERVAL = 60000;

	public class ServiceAccess extends Binder {

		public LocationLogger getService() {
			return LocationLogger.this;
		}

	}

	private final IBinder binder = new ServiceAccess();

	private boolean logging = false;

	private SendingStrategy sendingStrategy;

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
			addLocation(location);
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startLogging();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		sendingStrategy = new PeriodicSendingStrategy(new LocationDataSender(), this, LOCATION_SENDING_INTERVAL);
	}

	@Override
	public void onDestroy() {
		stopLogging();
	}

	public boolean isLogging() {
		return logging;
	}

	public void startLogging() {
		if (!logging) {
			logging = true;
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_INTERVAL, 0, locationListener);
			sendingStrategy.activate();
		}
	}

	public void stopLogging() {
		if (logging) {
			logging = false;
			locationManager.removeUpdates(locationListener);
			sendingStrategy.deactivate();
		}
	}
	
	private synchronized void addLocation(Location location) {
		if (locationHistory.size() == LOCATION_HISTORY_MAX_SIZE) {
			locationHistory.removeFirst();
		}
		locationHistory.addLast(location);
	}

	@Override
	public synchronized List<Location> getAndRemoveAllData() {
		List<Location> returnedLocations = locationHistory;
		locationHistory = new LinkedList<Location>();
		return returnedLocations;
	}

	
}
