package pl.edu.agh.assembler;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

import pl.edu.agh.model.LocationDataBatch;
import pl.edu.agh.model.LocationInfo;

public class LocationDataBatchAssembler {

	public static LocationDataBatch convert(List<Location> locationList) {
		List<LocationInfo> infos = new ArrayList<LocationInfo>();
		
		for (Location location : locationList) {
			infos.add(convert(location));
		}
		
		return new LocationDataBatch(infos);
	}

	private static LocationInfo convert(Location location) {
		LocationInfo info = new LocationInfo();
		
		info.setLongitude(location.getLongitude());
		info.setLatitude(location.getLatitude());
		info.setTime(location.getTime());
		info.setSpeed(location.getSpeed());
		info.setAccuracy(location.getAccuracy());
		info.setDirection(location.getBearing());
		
		return info;
	}
}
