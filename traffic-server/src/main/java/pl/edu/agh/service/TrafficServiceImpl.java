package pl.edu.agh.service;

import org.springframework.stereotype.Component;

import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationDataBatch;
import pl.edu.agh.model.TrafficData;

@Component
public class TrafficServiceImpl implements TrafficService {

	public void sendTrafficData(LocationDataBatch batch) throws JSONRPCException {
		// TODO Auto-generated method stub

	}

	public TrafficData getTrafficData(double latitude, double longitude) throws JSONRPCException {
		TrafficData td = new TrafficData();
		td.setNumber(7);
		return td;
	}

}
