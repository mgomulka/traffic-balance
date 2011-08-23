package pl.edu.agh.logic;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.edu.agh.adhoc.AdHocModule;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.jsonrpc.JSONRPCSocketServer;
import pl.edu.agh.model.RectD;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficData;
import pl.edu.agh.service.TrafficAdHocDispatchService;
import pl.edu.agh.service.TrafficService;
import pl.edu.agh.service.TrafficServiceStub;
import pl.edu.agh.utils.DateUtils;
import pl.edu.agh.utils.GeometryUtils;
import android.graphics.RectF;
import android.util.Log;

public class TrafficDataProvider extends AbstractProvider<TrafficDataListener> {

	private static final double OSMAND_BASE_RADIUS = 0.007186455;
	private static final double ZOOM_MULTIPLIER = 2.0;

	private static final int REFRESH_INTERVAL = 300;
	private static final int REFRESH_AFTER_ERROR_INTERVAL = 15;

	public static final int MIN_ZOOM = 11;
	public static final int MAX_ZOOM = 15;

	public enum Status {
		FETCHING, COMPLETED, ERROR;
	}

	private class TrafficDataRequest {
		private SimpleLocationInfo location;
		private int zoom;
		private Date requestTime;

		public TrafficDataRequest(SimpleLocationInfo location, int zoom, Date lastUpdate) {
			this.location = location;
			this.zoom = zoom;
			this.requestTime = lastUpdate;
		}

		public SimpleLocationInfo getLocation() {
			return location;
		}

		public int getZoom() {
			return zoom;
		}

		public Date getRequestTime() {
			return requestTime;
		}

	}

	private class TrafficDataSet {
		private TrafficData trafficData;
		private RectD bound;

		public TrafficDataSet(TrafficData trafficData, RectD bound) {
			this.trafficData = trafficData;
			this.bound = bound;
		}
		
		public TrafficDataSet() {
		}

		public TrafficData getTrafficData() {
			return trafficData;
		}

		public RectD getBound() {
			return bound;
		}

	}

	private TrafficService trafficService = TrafficServiceStub.getInstance();
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private final AdHocModule adHocModule;
	private final JSONRPCSocketServer adHocServer;
	private final TrafficAdHocDispatchService adHocDispatchService;
	private TrafficDataRequest lastRequest;
	private TrafficDataSet lastFetchedDataSet = new TrafficDataSet();
	private Status currentStatus;

	public TrafficDataProvider(AdHocModule adHocModule) {
		
		this.adHocModule = adHocModule;
		adHocDispatchService = new TrafficAdHocDispatchService(this, adHocModule.socket);
		trafficService.configAdHoc(adHocModule.socket, adHocDispatchService);
		adHocServer = new JSONRPCSocketServer(adHocModule.socket, adHocDispatchService);
		
	}
	
	public void startAdHocServer() {
		
		new Thread(new Runnable() {
			public void run() {
				try {

					adHocServer.start();
					
				} catch (JSONRPCException e) {
					
					Log.e("Ad Hoc Dispatch", e.getMessage(), e);
				}
			}
		}).start();

	}
	
	public void stopAdHocServer() {

		adHocServer.stop();
	}
	
	private void trafficDataProvided(final TrafficData trafficData) {
		forAllListeners(new ListenerTask<TrafficDataListener>() {

			@Override
			public void execute(TrafficDataListener listener) {
				listener.onDataProvided(trafficData);

			}
		});
	}

	private void statusChanged(final Status status) {
		forAllListeners(new ListenerTask<TrafficDataListener>() {

			@Override
			public void execute(TrafficDataListener listener) {
				listener.onStatusChanged(status);

			}
		});

	}

	public synchronized TrafficData getTrafficData(SimpleLocationInfo location, int zoom, RectF bound) {
		if (currentStatus != Status.FETCHING) {
			TrafficDataRequest newRequest = new TrafficDataRequest(location, zoom, new Date());

			if (newRequestRequired(lastRequest, lastFetchedDataSet.getBound(), newRequest, new RectD(bound))) {
				requestData(newRequest);
			}
		}

		return lastFetchedDataSet.getTrafficData();
	}

	public synchronized TrafficData getCachedTrafficDataIfAvailable(SimpleLocationInfo location, double radius) {
		if(lastRequest == null || lastFetchedDataSet == null) {
			return null;
		}
		
		SimpleLocationInfo cachedLocation = lastRequest.getLocation();
		double cachedRadius = calculateRadiusForZoom(lastRequest.getZoom());
		
		if((GeometryUtils.euklideanDist(location, cachedLocation) < cachedRadius/4.0)
				&& radius <= cachedRadius*1.25) {
			return lastFetchedDataSet.getTrafficData();
		} else {
			return null;
		}
	}
	
	private synchronized void updateLastRequest(Status status, TrafficDataRequest request, TrafficDataSet fetchedDataSet) {
		lastRequest = new TrafficDataRequest(request.getLocation(), request.getZoom(), new Date());
		currentStatus = status;
		lastFetchedDataSet = fetchedDataSet;
	}

	private boolean newRequestRequired(TrafficDataRequest previousRequest, RectD previousBound,
			TrafficDataRequest currentRequest, RectD currentBound) {
		if (previousRequest == null) {
			return true;
		}

		if (currentStatus == Status.ERROR) {
			return currentRequest.getRequestTime().after(
					DateUtils.add(previousRequest.getRequestTime(), Calendar.SECOND, REFRESH_AFTER_ERROR_INTERVAL));
		}

		if (currentRequest.getRequestTime().after(
				DateUtils.add(previousRequest.getRequestTime(), Calendar.SECOND, REFRESH_INTERVAL))) {
			return true;
		}

		if ((currentRequest.getZoom() != previousRequest.getZoom())
				&& !(currentRequest.getZoom() < MIN_ZOOM && previousRequest.getZoom() < MIN_ZOOM)
				&& !(currentRequest.getZoom() >= MAX_ZOOM && previousRequest.getZoom() >= MAX_ZOOM)) {
			return true;
		}

		return !previousBound.contains(currentBound);
	}

	private void requestData(final TrafficDataRequest request) {
		currentStatus = Status.FETCHING;
		executor.execute(new Runnable() {

			@Override
			public void run() {
				statusChanged(Status.FETCHING);

				double radius = calculateRadiusForZoom(request.getZoom());

				TrafficData fetchedData;
				Status status;

				try {
					fetchedData = request.getZoom() < MIN_ZOOM ? null : trafficService.getTrafficData(
							request.getLocation(), radius);
					status = Status.COMPLETED;
				} catch (JSONRPCException e) {

					try {
						fetchedData = alternativeRequestData(request);
						status = Status.COMPLETED;
						
					} catch (JSONRPCException e2) {
						fetchedData = null;
						status = Status.ERROR;
					}
					
				}
				updateLastRequest(status, request,
						new TrafficDataSet(fetchedData, GeometryUtils.createBoundingBox(request.getLocation(), radius)));
				statusChanged(status);
				trafficDataProvided(fetchedData);
			}

		});
	}
	
	private TrafficData alternativeRequestData(final TrafficDataRequest request) throws JSONRPCException {

		if(adHocModule.isProcessRunning()) {
			return trafficService.getTrafficDataViaAdHoc(request.getLocation(), calculateRadiusForZoom(request.getZoom()));
		} 
		throw new JSONRPCException("Ad Hoc not available");
	}
	
	

	private double calculateRadiusForZoom(int zoom) {
		zoom = Math.min(zoom, MAX_ZOOM);
		return OSMAND_BASE_RADIUS * 2 * Math.pow(ZOOM_MULTIPLIER, MAX_ZOOM - zoom);
	}
}
