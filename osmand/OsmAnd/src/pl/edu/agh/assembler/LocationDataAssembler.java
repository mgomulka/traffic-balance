package pl.edu.agh.assembler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.LocationInfo;
import android.location.Location;

public class LocationDataAssembler {

	public LocationData convert(List<Location> locationList) {
		List<LocationInfo> infos = new ArrayList<LocationInfo>();
		
		for (Location location : locationList) {
			infos.add(convert(location));
		}
		
		return new LocationData(infos);
	}

	private LocationInfo convert(Location location) {
		LocationInfo info = new LocationInfo();
		
		info.setLongitude(location.getLongitude());
		info.setLatitude(location.getLatitude());
		info.setTime(new Date(location.getTime()));
		
		return info;
	}
}
