package pl.edu.agh.service;

import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationDataBatch;
import pl.edu.agh.model.TrafficData;

public interface TrafficService {

	public static final String SEND_TRAFFIC_DATA_METHOD = "sendData";
	public static final String GET_TRAFFIC_DATA_METHOD = "getData";
	public static final String SERVICE_NAME = "trafficService";

	public void sendTrafficData(LocationDataBatch batch) throws JSONRPCException;

	public TrafficData getTrafficData(double latitude, double longitude) throws JSONRPCException;

}
