package pl.edu.agh.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.assembler.SimpleLocationInfoJSONAssembler;
import pl.edu.agh.assembler.TrafficDataJSONAssembler;
import pl.edu.agh.assembler.TrafficInfoJSONAssembler;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.jsonrpc.JSONRPCSkeleton;
import pl.edu.agh.jsonrpc.JSONRPCSocketClient;
import pl.edu.agh.jsonrpc.RawDataSocket;
import pl.edu.agh.logic.TrafficDataProvider;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficData;
import android.util.Log;

public class TrafficAdHocDispatchService extends JSONRPCSkeleton implements AsyncDataListener {

	private AsyncDataReceiver asyncReceiver = null;
	private final TrafficDataProvider trafficDataProvider;
	private final SimpleLocationInfoJSONAssembler locationInfoAssembler = new SimpleLocationInfoJSONAssembler();
	private final TrafficInfoJSONAssembler trafficInfoAssembler;
	private final TrafficDataJSONAssembler trafficDataAssembler;
	private final JSONRPCSocketClient rpcAdHocClient;
	
	public TrafficAdHocDispatchService(TrafficDataProvider provider, RawDataSocket socket) {
		this.trafficDataProvider = provider;
		this.rpcAdHocClient = new JSONRPCSocketClient(socket);
		this.trafficInfoAssembler = new TrafficInfoJSONAssembler(locationInfoAssembler);
		this.trafficDataAssembler = new TrafficDataJSONAssembler(trafficInfoAssembler);
	}
	
	public synchronized void registerReceiver(AsyncDataReceiver receiver) {
		this.asyncReceiver = receiver;
	}
	
	@Override
	protected JSONObject invoke(String methodName, JSONArray params)
			throws Exception {
		
		if(methodName.equals(TrafficService.ASYNC_TRAFFIC_DATA_EVENT)) {
			
			this.responseReceived(params);
			
		} else if(methodName.equals(TrafficService.GET_TRAFFIC_DATA_METHOD)) {

			this.requestReceived(params);
		}
		return null;
	}

	private synchronized void responseReceived(JSONArray params) throws JSONException {
		if(asyncReceiver != null) {
		
			JSONObject object = params.getJSONObject(0);
			Long requestId = params.getLong(1);
			
			if(asyncReceiver.getRequestCode() == requestId.longValue()) {
				asyncReceiver.dataReceived(object);
				this.asyncReceiver = null;
			}
		}
	}

	
	private synchronized void requestReceived(JSONArray params) throws JSONException {
	
		JSONObject serializedLocation = params.getJSONObject(0);
		SimpleLocationInfo location = locationInfoAssembler.deserialize(serializedLocation);
		Double radius = params.getDouble(1);
		Long requestId = params.getLong(2);
		
		TrafficData data = trafficDataProvider.getCachedTrafficDataIfAvailable(location, radius);
		
		if(data != null) {
			
			JSONObject locationData = trafficDataAssembler.serialize(data);
			try {
			
				rpcAdHocClient.call(TrafficService.ASYNC_TRAFFIC_DATA_EVENT, locationData, requestId);
			
			} catch (JSONRPCException e) {
			
				Log.e("Ad Hoc Dispatch", e.getMessage(), e);
			}
		}
	}
}
