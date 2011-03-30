package pl.edu.agh.model;

import java.util.List;

public class LocationDataBatch {

	private List<LocationInfo> locationInfos;
	// tutaj mozliwosc dodania innych pol, np userId

	public List<LocationInfo> getLocationInfos() {
		return locationInfos;
	}

	public LocationDataBatch(List<LocationInfo> locationInfos) {
		super();
		this.locationInfos = locationInfos;
	}

}
