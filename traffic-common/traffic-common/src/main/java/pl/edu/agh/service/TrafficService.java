package pl.edu.agh.service;

import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.RoutingResult;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficData;

public interface TrafficService {

	public static final String SERVICE_NAME = "trafficService";

	public static final String GET_TRAFFIC_DATA_METHOD = "getData";
	public static final String CALCULATE_ROUTE_METHOD = "calcRoute";
	
	public static final String NO_START_ROUTE_ERROR = "noStartRoute";
	public static final String NO_END_ROUTE_ERROR = "noEndRoute";
	public static final String CALCULATING_ROUTE_ERROR = "calculatingError";

	public TrafficData getTrafficData(SimpleLocationInfo location) throws JSONRPCException;

	public RoutingResult calculateRoute(SimpleLocationInfo start, SimpleLocationInfo end) throws JSONRPCException;

}
