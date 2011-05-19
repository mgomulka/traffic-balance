package pl.edu.agh.service;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.assembler.RoutingResultJSONAssembler;
import pl.edu.agh.assembler.SimpleLocationInfoJSONAssembler;
import pl.edu.agh.assembler.TrafficDataJSONAssembler;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.RoutingResult;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficData;

public class TrafficServiceStub extends AbstractServiceStub implements TrafficService {

	private TrafficDataJSONAssembler trafficDataJSONAssembler;
	private SimpleLocationInfoJSONAssembler simpleLocationInfoJSONAssembler;
	private RoutingResultJSONAssembler routingResultJSONAssembler;
	
	public static final TrafficServiceStub INSTANCE = new TrafficServiceStub();

	public TrafficServiceStub() {
		super(SERVICE_NAME);
		trafficDataJSONAssembler = new TrafficDataJSONAssembler();
		simpleLocationInfoJSONAssembler = new SimpleLocationInfoJSONAssembler();
		routingResultJSONAssembler = new RoutingResultJSONAssembler(simpleLocationInfoJSONAssembler);
	}

	@Override
	public RoutingResult calculateRoute(SimpleLocationInfo start, SimpleLocationInfo end) throws JSONRPCException {
		try {
			JSONObject serializedStart = simpleLocationInfoJSONAssembler.serialize(start);
			JSONObject serializedEnd = simpleLocationInfoJSONAssembler.serialize(end);
			JSONObject serializedRoutingResult = rpcClient.callJSONObject(CALCULATE_ROUTE_METHOD, serializedStart,
					serializedEnd);
			return routingResultJSONAssembler.deserialize(serializedRoutingResult);
		} catch (JSONException ex) {
			throw new JSONRPCException("Error during (de)serialization", ex);
		}
	}

	@Override
	public TrafficData getTrafficData(SimpleLocationInfo location) throws JSONRPCException {
		try {
			JSONObject serializedLocation = simpleLocationInfoJSONAssembler.serialize(location);
			JSONObject serializedData = rpcClient.callJSONObject(GET_TRAFFIC_DATA_METHOD, serializedLocation);
			return trafficDataJSONAssembler.deserialize(serializedData);
		} catch (JSONException ex) {
			throw new JSONRPCException("Error during (de)serialization", ex);
		}
	}

}
