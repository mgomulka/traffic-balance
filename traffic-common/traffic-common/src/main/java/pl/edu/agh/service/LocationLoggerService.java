package pl.edu.agh.service;

import java.util.List;

import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.RoutingResult;

public interface LocationLoggerService {
	
	public static final String SERVICE_NAME = "locationLoggerService";
	
	public static final String SEND_LOCATION_DATA_METHOD = "sendData";
	
	//zwracanie RoutingResult tylko na potrzeby testow - pozniej powinno byc void
	public List<RoutingResult> sendLocationData(LocationData locationData) throws JSONRPCException;

}
