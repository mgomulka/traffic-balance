package pl.edu.agh.logic;

import java.util.ArrayList;
import java.util.List;

import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.LocationInfo;
import pl.edu.agh.model.RoutingResult;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.service.LocationLoggerService;
import pl.edu.agh.service.LocationLoggerServiceStub;

public class LocationBuffer {

	public static final LocationBuffer INSTANCE = new LocationBuffer();

	private List<LocationInfo> locations = new ArrayList<LocationInfo>();
	private List<RoutingResult> calculatedRoute;
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

	public synchronized List<RoutingResult> getCalculatedRoute() {
		return calculatedRoute;
	}

	public synchronized void setCalculatedRoute(List<RoutingResult> route) {
		this.calculatedRoute = route;
	}

	public void sendLocations() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					List<RoutingResult> calc = locationLoggerService.sendLocationData(new LocationData(getLocations()));
					setCalculatedRoute(calc);
				} catch (JSONRPCException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

}
