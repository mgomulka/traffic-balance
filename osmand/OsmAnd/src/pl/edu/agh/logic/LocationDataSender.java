package pl.edu.agh.logic;

import java.util.List;

import pl.edu.agh.assembler.LocationDataAssembler;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationData;
import pl.edu.agh.service.LocationLoggerService;
import pl.edu.agh.service.LocationLoggerServiceStub;
import android.location.Location;
import android.util.Log;

public class LocationDataSender {
	
	private LocationLoggerService locationLoggerService = LocationLoggerServiceStub.getInstance();
	private LocationDataAssembler locationDataAssembler = new LocationDataAssembler();

	public void sendAllData(List<Location> locations) {
		LocationData locationData = locationDataAssembler.convert(locations);
		try {
			locationLoggerService.sendLocationData(locationData);
		} catch (JSONRPCException e) {
			Log.e(getClass().getName(), "Sending location data failed");
		}
	}
}
