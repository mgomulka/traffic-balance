package pl.edu.agh.model;

import java.util.List;

public class TrafficInfo {

	private List<SimpleLocationInfo> way;
	private Double directWaySpeed;
	private Double reverseWaySpeed;
	private boolean oneWay;

	public List<SimpleLocationInfo> getWay() {
		return way;
	}

	public Double getDirectWaySpeed() {
		return directWaySpeed;
	}

	public Double getReverseWaySpeed() {
		return reverseWaySpeed;
	}

	public boolean isOneWay() {
		return oneWay;
	}
	
	public TrafficInfo(List<SimpleLocationInfo> way, Double directWaySpeed, Double reverseWaySpeed, boolean oneWay) {
		this.way = way;
		this.directWaySpeed = directWaySpeed;
		this.reverseWaySpeed = reverseWaySpeed;
		this.oneWay = oneWay;
	}

}
