package pl.edu.agh.cache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficData;
import pl.edu.agh.service.TrafficService;
import pl.edu.agh.service.TrafficServiceStub;
import pl.edu.agh.utils.DateUtils;

public class TrafficDataProvider {
	
	public enum Status {
		FETCHING, COMPLETED, ERROR;
	}
	
	private class TrafficDataRequest {
		private SimpleLocationInfo location;
		private double zoom;
		private Date requestTime;

		private TrafficDataRequest(SimpleLocationInfo location, double zoom, Date lastUpdate) {
			this.location = location;
			this.zoom = zoom;
			this.requestTime = lastUpdate;
		}

		public SimpleLocationInfo getLocation() {
			return location;
		}

		public double getZoom() {
			return zoom;
		}

		public Date getRequestTime() {
			return requestTime;
		}

	}

	private static TrafficDataProvider INSTANCE = new TrafficDataProvider();
	public static TrafficDataProvider getInstance() {
		return INSTANCE;
	}
	
	private List<TrafficDataListener> listeners;
	private TrafficService trafficService = TrafficServiceStub.getInstance();
	
	private TrafficDataRequest lastRequest;
	private Status currentStatus;
	
	private TrafficDataProvider() {
		listeners = new ArrayList<TrafficDataListener>();
	}
	
	public void registerListener(TrafficDataListener listener) {
		synchronized (listeners) {
			listeners.add(listener);			
		}
	}
	
	private void trafficDataProvided(TrafficData data) {
		synchronized (listeners) {
			for (TrafficDataListener listener : listeners) {
				listener.onDataProvided(data);
			}
		}
	}
	
	private void statusChanged(Status status) {
		synchronized (listeners) {
			for (TrafficDataListener listener : listeners) {
				listener.onStatusChanged(status);
			}
		}
	}
	
	public synchronized void getTrafficDataAsync(SimpleLocationInfo location, double zoom) {
		if (currentStatus == Status.FETCHING) {
			return;
		}
		
		TrafficDataRequest newRequest = new TrafficDataRequest(location, zoom, new Date());
		if (lastRequest == null || newRequestRequired(lastRequest, newRequest)) {
			requestData(newRequest);
		}
	}
	
	private synchronized void updateLastRequestAndStatus(Status status, TrafficDataRequest request) {
		lastRequest = new TrafficDataRequest(request.getLocation(), request.getZoom(), new Date());
		currentStatus = status;
	}
	
	private boolean newRequestRequired(TrafficDataRequest lastRequest, TrafficDataRequest currentRequest) {
		return currentRequest.getRequestTime().after(DateUtils.add(lastRequest.getRequestTime(), Calendar.SECOND, 10));
	}
	
	private void requestData(final TrafficDataRequest request) {
		currentStatus = Status.FETCHING;
		new Thread(new Runnable() {

			@Override
			public void run() {
				statusChanged(Status.FETCHING);
				
				TrafficData fetchedData;
				Status status;
				
				try {
					fetchedData = trafficService.getTrafficData(request.getLocation());
					status = Status.COMPLETED;
					trafficDataProvided(fetchedData);
				} catch (JSONRPCException e) {
					fetchedData = null;
					status = Status.ERROR;
				}
				statusChanged(status);
				
				updateLastRequestAndStatus(status, request);
			}
		}).start();
	}
}
