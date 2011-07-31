package pl.edu.agh.service;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.assembler.LocationDataJSONAssembler;
import pl.edu.agh.assembler.LocationInfoJSONAssembler;
import pl.edu.agh.assembler.RoutingResultJSONAssembler;
import pl.edu.agh.assembler.SimpleLocationInfoJSONAssembler;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.RoutingResult;

public class LocationLoggerServiceStub extends AbstractServiceStub implements LocationLoggerService {

	private static final LocationLoggerServiceStub INSTANCE = new LocationLoggerServiceStub();
	public static LocationLoggerServiceStub getInstance() {
		return INSTANCE;
	}
	
	private LocationDataJSONAssembler locationDataJSONAssembler;
	private RoutingResultJSONAssembler routingResultJSONAssembler;
	
	private LocationLoggerServiceStub() {
		super(SERVICE_NAME);
		SimpleLocationInfoJSONAssembler simpleLocationInfoAssembler = new SimpleLocationInfoJSONAssembler();
		this.locationDataJSONAssembler = new LocationDataJSONAssembler(new LocationInfoJSONAssembler(
				simpleLocationInfoAssembler));
		this.routingResultJSONAssembler = new RoutingResultJSONAssembler(simpleLocationInfoAssembler);
	}

	@Override
	public List<RoutingResult> sendLocationData(LocationData locationData) throws JSONRPCException {
		try {
			JSONObject serializedData = locationDataJSONAssembler.serialize(locationData);
			JSONObject serializedRoutingResult = rpcClient.callJSONObject(SEND_LOCATION_DATA_METHOD, serializedData);
			return routingResultJSONAssembler.deserialize(serializedRoutingResult.getJSONArray("ala"));
			
		} catch (JSONException ex) {
			throw new JSONRPCException("Error during (de)serialization", ex);
		}

	}

}
