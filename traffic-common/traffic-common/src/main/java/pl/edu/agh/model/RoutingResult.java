package pl.edu.agh.model;

import java.util.List;

public class RoutingResult {
	
	private List<SimpleLocationInfo> locations;
	private List<SimpleLocationInfo> matchedPoints; //t

	public RoutingResult(List<SimpleLocationInfo> locations) {
		this.locations = locations;
	}

	public List<SimpleLocationInfo> getLocations() {
		return locations;
	}

	//t
	public List<SimpleLocationInfo> getMatchedPoints() {
		return matchedPoints;
	}

	public void setMatchedPoints(List<SimpleLocationInfo> matchedPoints) {
		this.matchedPoints = matchedPoints;
	}
	
	

}
