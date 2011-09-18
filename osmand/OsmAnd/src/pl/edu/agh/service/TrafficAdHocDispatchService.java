package pl.edu.agh.service;

import java.util.List;
import java.util.Random;

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
import pl.edu.agh.logic.TrafficDataBuffer;
import pl.edu.agh.logic.TrafficDataProvider;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficData;
import pl.edu.agh.model.TrafficInfo;
import android.util.Log;

public class TrafficAdHocDispatchService extends JSONRPCSkeleton implements AsyncDataListener {

	public static final int MAXIMUM_MESSAGE_SIZE = 1400;
	
	private AsyncDataReceiver asyncReceiver = null;
	
	private final TrafficDataProvider trafficDataProvider;
	private final SimpleLocationInfoJSONAssembler locationInfoAssembler = new SimpleLocationInfoJSONAssembler();
	private final TrafficInfoJSONAssembler trafficInfoAssembler;
	private final TrafficDataJSONAssembler trafficDataAssembler;
	private final JSONRPCSocketClient rpcAdHocClient;
	private final TrafficDataBuffer trafficDataBuffer = new TrafficDataBuffer();
	
	private int allowedMessageSize = MAXIMUM_MESSAGE_SIZE;
	
	public TrafficAdHocDispatchService(TrafficDataProvider provider, RawDataSocket socket) {
		this.trafficDataProvider = provider;
		this.rpcAdHocClient = new JSONRPCSocketClient(socket);
		this.trafficInfoAssembler = new TrafficInfoJSONAssembler(locationInfoAssembler);
		this.trafficDataAssembler = new TrafficDataJSONAssembler(trafficInfoAssembler);
	}
	
	public void setMessageSize(int size) {
		this.allowedMessageSize = size;
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
		
			if(!asyncReceiver.isInterested()) {
				trafficDataBuffer.discard();
				asyncReceiver = null;
			}
			
			JSONObject object = params.getJSONObject(0);
			Long requestId = params.getLong(1);
			Long sender = params.getLong(2);
			Integer seq = params.getInt(3);
			Boolean lastChunk = params.getBoolean(4);
			
			if(asyncReceiver.getRequestCode() == requestId.longValue()) {
				TrafficData dataChunk = trafficDataAssembler.deserialize(object);
				boolean completed = trafficDataBuffer.appendChunk(dataChunk, sender, seq, lastChunk);
				
				if(completed) {
					TrafficData data = trafficDataBuffer.getCollectedData().get(0);
					asyncReceiver.dataReceived(data);
					
					trafficDataBuffer.discard();
					asyncReceiver = null;
				}
			}
		}
	}

	
	private void requestReceived(JSONArray params) throws JSONException {
	
		JSONObject serializedLocation = params.getJSONObject(0);
		SimpleLocationInfo location = locationInfoAssembler.deserialize(serializedLocation);
		Double radius = params.getDouble(1);
		Long requestId = params.getLong(2);
		
		TrafficData data = trafficDataProvider.getCachedTrafficDataIfAvailable(location, radius);
		
		if(data != null) {
			
			JSONArray infosArray = new JSONArray();
			long senderId = new Random(System.currentTimeMillis()).nextLong();
			boolean lastChunk = false;
			int seq = 0;
			
			List<TrafficInfo> trafficInfos = data.getTrafficInfos();
			
			for(int i=0; i < trafficInfos.size(); i++) {
				
				TrafficInfo info = trafficInfos.get(i);
				if(i == trafficInfos.size()-1) {
					lastChunk = true;
				}

				JSONObject infoObject =  trafficInfoAssembler.serialize(info);

				int objectSize = infoObject.toString().length();
				int totalSize = infosArray.toString().length();
				
				if(totalSize + objectSize > allowedMessageSize) {

					
					JSONObject locationData = trafficDataAssembler.serialize(infosArray);
					
					sendData(locationData, requestId, senderId, seq, lastChunk);
					
					infosArray = new JSONArray();
					seq++;
					
				}
				infosArray.put(infoObject);
				
				if(lastChunk) {
					JSONObject locationData = trafficDataAssembler.serialize(infosArray);
					sendData(locationData, requestId, senderId, seq, lastChunk);
				}
			}
		}
	}
	
	private void sendData(JSONObject locationData, Long requestId, Long senderId,
			Integer seq, Boolean lastChunk) {
		try {
			
			rpcAdHocClient.call(TrafficService.ASYNC_TRAFFIC_DATA_EVENT, 
					locationData, requestId, senderId, seq, lastChunk);
			Thread.sleep(10);
		} catch (JSONRPCException e) {
		
			Log.e("Ad Hoc Dispatch", e.getMessage(), e);
		} catch (InterruptedException e) {
			Log.w("Ad Hoc Dispatch", e.getMessage(), e);
		}
	}
}
