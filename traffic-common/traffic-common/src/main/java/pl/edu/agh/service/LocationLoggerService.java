package pl.edu.agh.service;

import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationData;

public interface LocationLoggerService {

	public static final String SERVICE_NAME = "locationLoggerService";

	public static final String SEND_LOCATION_DATA_METHOD = "sendData";

	public void sendLocationData(LocationData locationData) throws JSONRPCException;

}
