package pl.edu.agh.service;

import java.util.Random;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.assembler.RoutingResultJSONAssembler;
import pl.edu.agh.assembler.SimpleLocationInfoJSONAssembler;
import pl.edu.agh.assembler.TrafficDataJSONAssembler;
import pl.edu.agh.assembler.TrafficInfoJSONAssembler;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.jsonrpc.JSONRPCSocketClient;
import pl.edu.agh.jsonrpc.RawDataSocket;
import pl.edu.agh.model.RoutingResult;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficData;
import android.util.Log;

public class TrafficServiceStub extends AbstractServiceStub implements TrafficService, AsyncDataReceiver {

	public static final int AD_HOC_WAITING_FOR_RESPONSE_TIMEOUT = 30*100000; 
	
	private TrafficDataJSONAssembler trafficDataJSONAssembler;
	private SimpleLocationInfoJSONAssembler simpleLocationInfoJSONAssembler;
	private RoutingResultJSONAssembler routingResultJSONAssembler;
	private AsyncDataListener asyncDataListener;
	
	private boolean waitingForBroadCast = false;
	private TrafficData lastReceivedAsyncData = null;
	private long lastRequestId = 0;
	
	private static final TrafficServiceStub INSTANCE = new TrafficServiceStub();
	public static TrafficServiceStub getInstance() {
		return INSTANCE;
	}

	public static TrafficServiceStub getCopy() {
		return new TrafficServiceStub();
	}
	
	private TrafficServiceStub() {
		super(SERVICE_NAME);
		simpleLocationInfoJSONAssembler = new SimpleLocationInfoJSONAssembler();
		routingResultJSONAssembler = new RoutingResultJSONAssembler(simpleLocationInfoJSONAssembler);
		trafficDataJSONAssembler = new TrafficDataJSONAssembler(new TrafficInfoJSONAssembler(
				simpleLocationInfoJSONAssembler));
	}

	@Override
	public RoutingResult calculateRoute(SimpleLocationInfo start, SimpleLocationInfo end, boolean useTrafficDataToRoute) throws JSONRPCException {
		try {
			JSONObject serializedStart = simpleLocationInfoJSONAssembler.serialize(start);
			JSONObject serializedEnd = simpleLocationInfoJSONAssembler.serialize(end);
			JSONObject serializedRoutingResult = rpcClient.callJSONObject(CALCULATE_ROUTE_METHOD, serializedStart,
					serializedEnd, useTrafficDataToRoute);
			return routingResultJSONAssembler.deserialize(serializedRoutingResult);
		} catch (JSONException ex) {
			throw new JSONRPCException("Error during (de)serialization", ex);
		}
	}

	@Override
	public TrafficData getTrafficData(SimpleLocationInfo location, double radius) throws JSONRPCException {
		try {
			JSONObject serializedLocation = simpleLocationInfoJSONAssembler.serialize(location);
			JSONObject serializedData = rpcClient.callJSONObject(GET_TRAFFIC_DATA_METHOD, serializedLocation, radius);
			return trafficDataJSONAssembler.deserialize(serializedData);
		} catch (JSONException ex) {
			throw new JSONRPCException("Error during (de)serialization", ex);
		}
	}

	public synchronized void dataReceived(TrafficData data) {
		lastReceivedAsyncData = data;
		waitingForBroadCast = false;
		this.notify();
	}
	
	public synchronized boolean isInterested() {
		return waitingForBroadCast;
	}
	
	public long getRequestCode() {
		return lastRequestId;
	}
	
	@Override
	public TrafficData getTrafficDataViaAdHoc(SimpleLocationInfo location, double radius) throws JSONRPCException {
		try {
			lastRequestId = new Random(System.currentTimeMillis()).nextLong();
			JSONObject serializedLocation = simpleLocationInfoJSONAssembler.serialize(location);
			
			Runnable timeoutTask = new Runnable() {
				public void run() {
					try {
						
						Thread.sleep(AD_HOC_WAITING_FOR_RESPONSE_TIMEOUT);
						
					} catch(InterruptedException e) {
						Log.i(Thread.currentThread().getName(), "thread interrupted", e);
					} finally {
						
						synchronized (TrafficServiceStub.this) {
							waitingForBroadCast = false;
							TrafficServiceStub.this.notify();
						}
					}
				}
			};
			
			waitingForBroadCast = true;
			Executors.newSingleThreadExecutor().execute(timeoutTask);
			asyncDataListener.registerReceiver(this);
			rpcAdHocClient.callJSONObject(GET_TRAFFIC_DATA_METHOD, serializedLocation, radius, lastRequestId);
			
			synchronized (this) {
				try {
					while(waitingForBroadCast) {
						this.wait();
					}
				} catch(InterruptedException e) {
					Log.i(Thread.currentThread().getName(), "thread interrupted", e);
				}
				TrafficData response = lastReceivedAsyncData;
				lastReceivedAsyncData = null;
				return response;
			}
			
		} catch (JSONException ex) {
			throw new JSONRPCException("Error during (de)serialization", ex);
		}

	}

	@Override
	public void configAdHoc(RawDataSocket socket, AsyncDataListener listener) {
		this.rpcAdHocClient = new JSONRPCSocketClient(socket);
		this.asyncDataListener = listener;
	}

}
