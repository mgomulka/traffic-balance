package pl.edu.agh.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.assembler.LocationDataJSONAssembler;
import pl.edu.agh.assembler.LocationInfoJSONAssembler;
import pl.edu.agh.assembler.SimpleLocationInfoJSONAssembler;
import pl.edu.agh.jsonrpc.JSONRPCClient;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.jsonrpc.JSONRPCHttpClient;
import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.LocationInfo;
import pl.edu.agh.service.LocationLoggerService;

public class LocationDataSender {

	private static final int LOCATION_HISTORY_MAX_SIZE = 1000;
	private static final long LOCATION_SENDING_INTERVAL = 60; //[s]
	
	protected final JSONRPCClient rpcClient;
	
	private int vehicleNum = 0;
	private long beginTime;
	private int sends = 0;
	private final Date startingTime;
	private Date currentTime;
	private long maxRequestTime = 0;
	
	private final LocationDataJSONAssembler locationDataJSONAssembler;

	private Map<String, LocationEntry> vehicles = new HashMap<String, LocationEntry>();
	
	public LocationDataSender(String serviceUrl, Date startingTime) {
		
		rpcClient = new JSONRPCHttpClient(serviceUrl);
		
		this.startingTime = startingTime;
		this.currentTime = startingTime;
		
		SimpleLocationInfoJSONAssembler simpleAssembler = new SimpleLocationInfoJSONAssembler();
		LocationInfoJSONAssembler locationInfoAssembler = new LocationInfoJSONAssembler(simpleAssembler);
		locationDataJSONAssembler = new LocationDataJSONAssembler(locationInfoAssembler);
		beginTime = System.currentTimeMillis();
	}
	
	public void put(String vehicleId, double lon, double lat, int time) {
		
		LocationEntry entry = vehicles.get(vehicleId);
		if(entry == null) {
			entry = new LocationEntry();
			vehicles.put(vehicleId, entry);
			vehicleNum++;
		}
		
		LocationInfo info = new LocationInfo();
		info.setLatitude(lat);
		info.setLongitude(lon);
		Date tempDate = new Date(); 
		tempDate.setTime(this.startingTime.getTime() + time * 1000);
		
		info.setTime(tempDate);
		entry.addLocInfo(info);
		entry.tick();
	}
	
	public void tick() {
		
		currentTime.setTime(this.startingTime.getTime() + 1000);
		for(LocationEntry entry : vehicles.values()) {
			if(entry.isReady()) {
				
				LocationData data = entry.getData();
				entry.clear();
				
				try {
					
					JSONObject object = locationDataJSONAssembler.serialize(data);
					long reqBegin = System.currentTimeMillis();
					rpcClient.callJSONObject(LocationLoggerService.SEND_LOCATION_DATA_METHOD, object);
					long reqTime = System.currentTimeMillis() - reqBegin;
					if(reqTime > maxRequestTime) {
						maxRequestTime = reqTime;
					}
					sends ++;
					
				} catch (JSONException e) {
					e.printStackTrace();
					System.exit(-3);
					
				} 
				catch (JSONRPCException e) {
					e.printStackTrace();
					System.exit(-2);
				}
			}
		}
	}
	
	public double getSendingRate() {
		return (double)sends/(double)(System.currentTimeMillis() - beginTime) * 1000.0;
	}
	public int getTotalCars() {
		return vehicleNum;
	}
	public long getMaxRequestTime() {
		return maxRequestTime;
	}
	
	public static class LocationEntry {
		
		private LocationData data = new LocationData(new ArrayList<LocationInfo>());
		private int ticks = 0;
		
		public boolean isReady() {
			
			return data.getLocationInfos().size() >= LOCATION_HISTORY_MAX_SIZE 
				|| ticks >= LOCATION_SENDING_INTERVAL;
		}
		
		public void addLocInfo(LocationInfo info) {
			this.data.getLocationInfos().add(info);
		}
		public void tick() {
			ticks++;
		}
		public void clear() {
			ticks = 0;
			data = new LocationData(new ArrayList<LocationInfo>());
		}
		public LocationData getData() {
			return this.data;
		}
	}
	
}
