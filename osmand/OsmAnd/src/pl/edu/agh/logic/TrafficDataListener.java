package pl.edu.agh.logic;

import pl.edu.agh.model.TrafficData;

public interface TrafficDataListener {

	public void onDataProvided(TrafficData trafficData);
	public void onStatusChanged(TrafficDataProvider.Status status);
}
