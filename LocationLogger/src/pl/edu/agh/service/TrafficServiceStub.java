package pl.edu.agh.service;

import static pl.edu.agh.assembler.LocationDataBatchJSONAssembler.serialize;
import static pl.edu.agh.assembler.TrafficDataJSONAssembler.deserialize;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.jsonrpc.JSONRPCClient;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.jsonrpc.JSONRPCHttpClient;
import pl.edu.agh.model.LocationDataBatch;
import pl.edu.agh.model.TrafficData;

public class TrafficServiceStub implements TrafficService {

	private static final String SERVICE_URL_TEMPLATE = "http://%s/%s";
	
	private JSONRPCClient rpcClient;

	public TrafficServiceStub(String serverUrl) {
		rpcClient = new JSONRPCHttpClient(String.format(SERVICE_URL_TEMPLATE, serverUrl, SERVICE_NAME));
	}

	@Override
	public void sendTrafficData(LocationDataBatch batch) throws JSONRPCException {
		try {
			JSONObject serializedBatch = serialize(batch);
			rpcClient.callJSONObject(SEND_TRAFFIC_DATA_METHOD, serializedBatch);
		} catch (JSONException ex) {
			throw new JSONRPCException("Error during (de)serialization", ex);
		}
	}

	@Override
	public TrafficData getTrafficData(double latitude, double longitude) throws JSONRPCException {
		try {
			JSONObject serializedResult = rpcClient.callJSONObject(GET_TRAFFIC_DATA_METHOD, latitude, longitude);
			return deserialize(serializedResult);
		} catch (JSONException ex) {
			throw new JSONRPCException("Error during (de)serialization", ex);
		}
	}

}
