package pl.edu.agh.model;

import java.util.List;

public class RoutingResult {
	
	private List<SimpleLocationInfo> locations;

	public RoutingResult(List<SimpleLocationInfo> locations) {
		this.locations = locations;
	}

	public List<SimpleLocationInfo> getLocations() {
		return locations;
	}

}
