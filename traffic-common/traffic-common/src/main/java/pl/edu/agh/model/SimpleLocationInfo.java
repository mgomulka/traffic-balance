package pl.edu.agh.model;

public class SimpleLocationInfo {

	private double longitude;
	private double latitude;

	public SimpleLocationInfo(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public SimpleLocationInfo() {
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

}
