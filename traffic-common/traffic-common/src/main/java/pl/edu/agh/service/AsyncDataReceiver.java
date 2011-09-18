package pl.edu.agh.service;

import pl.edu.agh.model.TrafficData;

public interface AsyncDataReceiver {

	long getRequestCode();
	boolean isInterested();
	void dataReceived(TrafficData data);
}
