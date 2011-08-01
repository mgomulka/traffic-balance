package pl.edu.agh.service;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.assembler.LocationDataJSONAssembler;
import pl.edu.agh.assembler.LocationInfoJSONAssembler;
import pl.edu.agh.assembler.SimpleLocationInfoJSONAssembler;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.LocationData;

public class LocationLoggerServiceStub extends AbstractServiceStub implements LocationLoggerService {

	private static final LocationLoggerServiceStub INSTANCE = new LocationLoggerServiceStub();

	public static LocationLoggerServiceStub getInstance() {
		return INSTANCE;
	}

	private LocationDataJSONAssembler locationDataJSONAssembler;

	private LocationLoggerServiceStub() {
		super(SERVICE_NAME);
		this.locationDataJSONAssembler = new LocationDataJSONAssembler(new LocationInfoJSONAssembler(
				new SimpleLocationInfoJSONAssembler()));
	}

	@Override
	public void sendLocationData(LocationData locationData) throws JSONRPCException {
		try {
			JSONObject serializedData = locationDataJSONAssembler.serialize(locationData);
			rpcClient.callJSONObject(SEND_LOCATION_DATA_METHOD, serializedData);
		} catch (JSONException ex) {
			throw new JSONRPCException("Error during (de)serialization", ex);
		}

	}

}
