package pl.edu.agh.logic;

import java.util.ArrayList;
import java.util.List;

import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.LocationInfo;
import pl.edu.agh.service.LocationLoggerService;
import pl.edu.agh.service.LocationLoggerServiceStub;

public class LocationBuffer {

	public static final LocationBuffer INSTANCE = new LocationBuffer();

	private List<LocationInfo> locations = new ArrayList<LocationInfo>();
	private LocationLoggerService locationLoggerService = LocationLoggerServiceStub.getInstance();

	public synchronized void addLocation(LocationInfo location) {
		locations.add(location);
	}

	public synchronized List<LocationInfo> getLocations() {
		return locations;
	}
	
	public synchronized List<LocationInfo> getAndClearLocations() {
		List<LocationInfo> result = new ArrayList<LocationInfo>(locations);
		locations.clear();
		return result;
	}

	public void sendLocations() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					locationLoggerService.sendLocationData(new LocationData(getLocations()));
				} catch (JSONRPCException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

}
