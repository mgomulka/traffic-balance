package pl.edu.agh.model;

import java.util.List;

public class LocationData {

	private List<LocationInfo> locationInfos;
	// tutaj mozliwosc dodania innych pol, np userId

	public List<LocationInfo> getLocationInfos() {
		return locationInfos;
	}

	public LocationData(List<LocationInfo> locationInfos) {
		this.locationInfos = locationInfos;
	}

}
