package net.osmand.plus.activities;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.osmand.Algoritms;
import net.osmand.FavouritePoint;
import net.osmand.GPXUtilities;
import net.osmand.GPXUtilities.GPXFileResult;
import net.osmand.GPXUtilities.WptPt;
import net.osmand.LogUtil;
import net.osmand.OsmAndFormatter;
import net.osmand.Version;
import net.osmand.data.Amenity;
import net.osmand.data.AmenityType;
import net.osmand.data.preparation.MapTileDownloader;
import net.osmand.data.preparation.MapTileDownloader.DownloadRequest;
import net.osmand.data.preparation.MapTileDownloader.IMapDownloaderCallback;
import net.osmand.map.IMapLocationListener;
import net.osmand.map.ITileSource;
import net.osmand.osm.LatLon;
import net.osmand.osm.MapUtils;
import net.osmand.plus.AmenityIndexRepository;
import net.osmand.plus.AmenityIndexRepositoryOdb;
import net.osmand.plus.BusyIndicator;
import net.osmand.plus.FavouritesDbHelper;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.OsmandSettings.ApplicationMode;
import net.osmand.plus.PoiFilter;
import net.osmand.plus.PoiFiltersHelper;
import net.osmand.plus.R;
import net.osmand.plus.ResourceManager;
import net.osmand.plus.SQLiteTileSource;
import net.osmand.plus.activities.search.SearchActivity;
import net.osmand.plus.activities.search.SearchPoiFilterActivity;
import net.osmand.plus.activities.search.SearchTransportActivity;
import net.osmand.plus.render.MapRenderRepositories;
import net.osmand.plus.render.RendererLayer;
import net.osmand.plus.views.AGHTrafficLayer;
import net.osmand.plus.views.AnimateDraggingMapThread;
import net.osmand.plus.views.ContextMenuLayer;
import net.osmand.plus.views.FavoritesLayer;
import net.osmand.plus.views.GPXLayer;
import net.osmand.plus.views.MapInfoLayer;
import net.osmand.plus.views.OsmBugsLayer;
import net.osmand.plus.views.OsmandMapTileView;
import net.osmand.plus.views.POIMapLayer;
import net.osmand.plus.views.PointLocationLayer;
import net.osmand.plus.views.PointNavigationLayer;
import net.osmand.plus.views.RouteInfoLayer;
import net.osmand.plus.views.RouteLayer;
import net.osmand.plus.views.TransportInfoLayer;
import net.osmand.plus.views.TransportStopsLayer;
import net.osmand.plus.views.YandexTrafficLayer;
import pl.edu.agh.logic.LocationBuffer;
import pl.edu.agh.model.LocationInfo;
import pl.edu.agh.service.LocationLogger;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.ClipboardManager;
import android.text.Html;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ZoomControls;

public class MapActivity extends Activity implements IMapLocationListener, SensorEventListener {

	private static final String GPS_STATUS_ACTIVITY = "com.eclipsim.gpsstatus2.GPSStatus"; //$NON-NLS-1$
	private static final String GPS_STATUS_COMPONENT = "com.eclipsim.gpsstatus2"; //$NON-NLS-1$
	
	// stupid error but anyway hero 2.1 : always lost gps signal (temporarily unavailable) for timeout = 2000
	private static final int GPS_TIMEOUT_REQUEST = 1000;
	private static final int GPS_DIST_REQUEST = 5;
	// use only gps (not network) for 12 seconds 
	private static final int USE_ONLY_GPS_INTERVAL = 12000; 
	
	private boolean providerSupportsBearing = false;
	@SuppressWarnings("unused")
	private boolean providerSupportsSpeed = false;
	private String currentLocationProvider = null;
	private long lastTimeAutoZooming = 0;
	private long lastTimeGPXLocationFixed = 0;
	
    /** Called when the activity is first created. */
	private OsmandMapTileView mapView;
	
	private ImageButton backToLocation;
	private ImageButton backToMenu;
	
	// the order of layer should be preserved ! when you are inserting new layer
	private RendererLayer rendererLayer;
	private GPXLayer gpxLayer;
	private RouteLayer routeLayer;
	private YandexTrafficLayer trafficLayer;
	private OsmBugsLayer osmBugsLayer;
	private POIMapLayer poiMapLayer;
	private FavoritesLayer favoritesLayer;
	private TransportStopsLayer transportStopsLayer;
	private TransportInfoLayer transportInfoLayer;
	private PointLocationLayer locationLayer;
	private PointNavigationLayer navigationLayer;
	private MapInfoLayer mapInfoLayer;
	private ContextMenuLayer contextMenuLayer;
	private RouteInfoLayer routeInfoLayer;
	private AGHTrafficLayer aghTrafficLayer;
	
	private SavingTrackHelper savingTrackHelper;
	private RoutingHelper routingHelper;
	
	
	private WakeLock wakeLock;
	private boolean sensorRegistered = false;

	private NotificationManager mNotificationManager;
	private Handler mapPositionHandler = null;
	private int APP_NOTIFICATION_ID;
	private int currentScreenOrientation;
	private int currentMapRotation;
	private boolean currentShowingAngle;
	
	private OsmandApplication application;
	
	private Dialog progressDlg = null;
	private SharedPreferences settings;
	
/*	private boolean boundToLoggingService = false;
	private LocationLogger locationLogger;
	private ServiceConnection locationLoggerConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			boundToLoggingService = true;			
			locationLogger = ((ServiceAccess)service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			boundToLoggingService = false;
		}
	};
*/
	private boolean isMapLinkedToLocation(){
		return OsmandSettings.isMapSyncToGpsLocation(settings);
	}
	
	private Notification getNotification(){
		Intent notificationIndent = new Intent(this, MapActivity.class);
		notificationIndent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		Notification notification = new Notification(R.drawable.icon, "", //$NON-NLS-1$
				System.currentTimeMillis());
		notification.setLatestEventInfo(this, Version.APP_NAME,
				getString(R.string.go_back_to_osmand), PendingIntent.getActivity(
						this, 0, notificationIndent,
						PendingIntent.FLAG_UPDATE_CURRENT));
		return notification;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.application = (OsmandApplication)this.getApplication();
		settings = OsmandSettings.getPrefs(this);		
		// for voice navigation
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		// Full screen is not used here
//	     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);
		ProgressDialog dlg = ((OsmandApplication)getApplication()).checkApplicationIsBeingInitialized(this);
		if(dlg != null){
			// Do some action on close
			dlg.setOnDismissListener(new DialogInterface.OnDismissListener(){
				@Override
				public void onDismiss(DialogInterface dialog) {
					OsmandApplication app = ((OsmandApplication)getApplication());
					if(OsmandSettings.isUsingMapVectorData(settings) && app.getResourceManager().getRenderer().isEmpty()){
						Toast.makeText(MapActivity.this, getString(R.string.no_vector_map_loaded), Toast.LENGTH_LONG).show();
					}
				}
			});
		}
		parseLaunchIntentLocation();
		
		mapView = (OsmandMapTileView) findViewById(R.id.MapView);
		mapView.setTrackBallDelegate(new OsmandMapTileView.OnTrackBallListener(){
			@Override
			public boolean onTrackBallEvent(MotionEvent e) {
				showAndHideMapPosition();
				return MapActivity.this.onTrackballEvent(e);
			}

		});
		MapTileDownloader.getInstance().addDownloaderCallback(new IMapDownloaderCallback(){
			@Override
			public void tileDownloaded(DownloadRequest request) {
				if(request != null && !request.error && request.fileToSave != null){
					ResourceManager mgr = ((OsmandApplication)getApplication()).getResourceManager();
					mgr.tileDownloaded(request);
				}
				mapView.tileDownloaded(request);
				
			}
		});
		
		mapView.setMapLocationListener(this);
		routingHelper = ((OsmandApplication) getApplication()).getRoutingHelper();
		
		// 0.5 layer
		rendererLayer = new RendererLayer();
		mapView.addLayer(rendererLayer, 0.5f);
		
		// 0.6 gpx layer
		gpxLayer = new GPXLayer();
		mapView.addLayer(gpxLayer, 0.6f);
		
		//0.8 agh traffic layer
		aghTrafficLayer = new AGHTrafficLayer();
		
		// 1. route layer
		routeLayer = new RouteLayer(routingHelper);
		mapView.addLayer(routeLayer, 1);
		
		// 1.5. traffic layer
		trafficLayer = new YandexTrafficLayer();
		mapView.addLayer(trafficLayer, 1.5f);
		
		
		// 2. osm bugs layer
		osmBugsLayer = new OsmBugsLayer(this);
		// 3. poi layer
		poiMapLayer = new POIMapLayer();
		// 4. favorites layer
		favoritesLayer = new FavoritesLayer();
		// 5. transport layer
		transportStopsLayer = new TransportStopsLayer();
		// 5.5 transport info layer 
		transportInfoLayer = new TransportInfoLayer(TransportRouteHelper.getInstance());
		mapView.addLayer(transportInfoLayer, 5.5f);
		// 6. point navigation layer
		navigationLayer = new PointNavigationLayer();
		mapView.addLayer(navigationLayer, 6);
		// 7. point location layer 
		locationLayer = new PointLocationLayer();
		mapView.addLayer(locationLayer, 7);
		// 8. map info layer
		mapInfoLayer = new MapInfoLayer(this, routeLayer);
		mapView.addLayer(mapInfoLayer, 8);
		// 9. context menu layer 
		contextMenuLayer = new ContextMenuLayer(this);
		mapView.addLayer(contextMenuLayer, 9);
		// 10. route info layer
		routeInfoLayer = new RouteInfoLayer(routingHelper, (LinearLayout) findViewById(R.id.RouteLayout));
		mapView.addLayer(routeInfoLayer, 10);
		

		
		savingTrackHelper = new SavingTrackHelper(this);
		
		LatLon pointToNavigate = OsmandSettings.getPointToNavigate(settings);
		
		// This situtation could be when navigation suddenly crashed and after restarting
		// it tries to continue the last route
		if(!Algoritms.objectEquals(routingHelper.getFinalLocation(), pointToNavigate)){
			routingHelper.setFollowingMode(false);
			OsmandSettings.setFollowingByRoute(this, false);
			stopLogger();
			routingHelper.setFinalAndCurrentLocation(pointToNavigate, null);

		}
		if(OsmandSettings.isFollowingByRoute(settings)){
			if(pointToNavigate == null){
				OsmandSettings.setFollowingByRoute(this, false);
			} else if(!routingHelper.isRouteCalculated()){
				Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.continue_follow_previous_route);
				builder.setPositiveButton(R.string.default_buttons_yes, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						routingHelper.setFollowingMode(true);
						OsmandSettings.setFollowingByRoute(MapActivity.this, true);
						startLogger();
						((OsmandApplication)getApplication()).showDialogInitializingCommandPlayer(MapActivity.this);
					}
				});
				builder.setNegativeButton(R.string.default_buttons_no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						routingHelper.setFollowingMode(false);
						OsmandSettings.setFollowingByRoute(MapActivity.this, false);
						routingHelper.setFinalLocation(null);
						mapView.refreshMap();
					}
				});
				builder.show();
			}
		}
		
		navigationLayer.setPointToNavigate(pointToNavigate);
		
		SharedPreferences prefs = getSharedPreferences(OsmandSettings.SHARED_PREFERENCES_NAME, MODE_WORLD_READABLE);
		if(prefs == null || !prefs.contains(OsmandSettings.LAST_KNOWN_MAP_LAT)){
			LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
			Location location = null;
			try {
				location = service.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			catch(IllegalArgumentException e) {
				Log.d(LogUtil.TAG, "GPS location provider not available"); //$NON-NLS-1$
			}

			if(location == null){
				try {
					location = service.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				} catch(IllegalArgumentException e) {
					Log.d(LogUtil.TAG, "Network location provider not available"); //$NON-NLS-1$
				}
			}
			if(location != null){
				mapView.setLatLon(location.getLatitude(), location.getLongitude());
				mapView.setZoom(14);
			}
		}
		
		
		
		final ZoomControls zoomControls = (ZoomControls) findViewById(R.id.ZoomControls);
		zoomControls.setOnZoomInClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomControls.setIsZoomInEnabled(mapView.getZoom() + 1 < mapView.getMaximumShownMapZoom());
				zoomControls.setIsZoomOutEnabled(mapView.getZoom() + 1 > mapView.getMinimumShownMapZoom());
				mapView.getAnimatedDraggingThread().stopAnimatingSync();
				mapView.getAnimatedDraggingThread().startZooming(mapView.getZoom(), mapView.getZoom() + 1);
				showAndHideMapPosition();
				// user can preview map manually switch off auto zoom while user don't press back to location
				if(OsmandSettings.isAutoZoomEnabled(settings)){
					locationChanged(mapView.getLatitude(), mapView.getLongitude(), null);
				}
			}
		});
		zoomControls.setOnZoomOutClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomControls.setIsZoomInEnabled(mapView.getZoom() - 1 < mapView.getMaximumShownMapZoom());
				zoomControls.setIsZoomOutEnabled(mapView.getZoom() - 1 > mapView.getMinimumShownMapZoom());
				mapView.getAnimatedDraggingThread().stopAnimatingSync();
				mapView.getAnimatedDraggingThread().startZooming(mapView.getZoom(), mapView.getZoom() - 1);
				showAndHideMapPosition();
				// user can preview map manually switch off auto zoom while user don't press back to location
				if(OsmandSettings.isAutoZoomEnabled(settings)){
					locationChanged(mapView.getLatitude(), mapView.getLongitude(), null);
				}
			}
		});
		backToLocation = (ImageButton)findViewById(R.id.BackToLocation);
		backToLocation.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				backToLocationImpl();
			}
			
		});
		
		
		
		backToMenu = (ImageButton)findViewById(R.id.BackToMenu);
		backToMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent newIntent = new Intent(MapActivity.this, MainMenuActivity.class);
				newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(newIntent);
			}
		});
		
		// Possibly use that method instead of 
		/*mapView.setOnLongClickListener(new OsmandMapTileView.OnLongClickListener(){
			@Override
			public boolean onLongPressEvent(PointF point) {
				LatLon l = mapView.getLatLonFromScreenPoint(point.x, point.y);
				return true;
			}
		});*/
		
	}
    
 
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
			Intent newIntent = new Intent(MapActivity.this, SearchActivity.class);
			startActivity(newIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onTrackballEvent(MotionEvent event) {
    	if(event.getAction() == MotionEvent.ACTION_MOVE && OsmandSettings.isUsingTrackBall(settings)){
    		float x = event.getX();
    		float y = event.getY();
    		LatLon l = mapView.getLatLonFromScreenPoint(mapView.getCenterPointX() + x * 15, mapView.getCenterPointY() + y * 15);
    		setMapLocation(l.getLatitude(), l.getLongitude());
    		return true;
    	} 
    	return super.onTrackballEvent(event);
    }
    
	@Override
	protected void onStart() {
		super.onStart();

		/*if (!boundToLoggingService) {
			this.bindService(new Intent(this, LocationLogger.class), locationLoggerConnection, Context.BIND_AUTO_CREATE);
		}*/
	}
    
    @Override
    protected void onStop() {
    	// TODO keep this check?
    	if(routingHelper.isFollowingMode()){
    		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    		mNotificationManager.notify(APP_NOTIFICATION_ID, getNotification());
    	}
		if(progressDlg != null){
			progressDlg.dismiss();
			progressDlg = null;
		}
		/*if(boundToLoggingService) {
    		this.unbindService(locationLoggerConnection);
    	}*/
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	savingTrackHelper.close();
    	if(mNotificationManager != null){
    		mNotificationManager.cancel(APP_NOTIFICATION_ID);
    	}
    	MapTileDownloader.getInstance().removeDownloaderCallback(mapView);
    	if(mapView.getLayers().contains(aghTrafficLayer)) {
    		mapView.removeLayer(aghTrafficLayer);
    	}
    }
    
    
    
    private void registerUnregisterSensor(Location location){
    	boolean show = (currentShowingAngle && location != null) || currentMapRotation == OsmandSettings.ROTATE_MAP_COMPASS;
    	// show point view only if gps enabled
		if (sensorRegistered && !show) {
			Log.d(LogUtil.TAG, "Disable sensor"); //$NON-NLS-1$
			((SensorManager) getSystemService(SENSOR_SERVICE)).unregisterListener(this);
			sensorRegistered = false;
			locationLayer.setHeading(null);
		} else if (!sensorRegistered && show) {
			Log.d(LogUtil.TAG, "Enable sensor"); //$NON-NLS-1$
			SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
			Sensor s = sensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			if (s != null) {
				sensorMgr.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
			}
			sensorRegistered = true;
		}
    }
    
    private void updateSpeedBearing(Location location) {
		// For network/gps it's bad way (not accurate). It's widely used for testing purposes
    	// possibly keep using only for emulator case
//		if (!providerSupportsSpeed
    	if (isRunningOnEmulator()
    			&& locationLayer.getLastKnownLocation() != null && location != null) {
			if (locationLayer.getLastKnownLocation().distanceTo(location) > 3) {
				float d = location.distanceTo(locationLayer.getLastKnownLocation());
				long time = location.getTime() - locationLayer.getLastKnownLocation().getTime();
				float speed;
				if (time == 0) {
					speed = 0;
				} else {
					speed = ((float) d * 1000) / time ;
				}
				// incorrect in case of airplane
				if (speed > 100) {
					speed = 100;
				}
				location.setSpeed(speed);
			}
		}
    	if(!providerSupportsBearing && locationLayer.getLastKnownLocation() != null && location != null){
    		if(locationLayer.getLastKnownLocation().distanceTo(location) > 10){
    			location.setBearing(locationLayer.getLastKnownLocation().bearingTo(location));
    		}
    	}
	}
    
    public void setLocation(Location location){
    	if(Log.isLoggable(LogUtil.TAG, Log.DEBUG)){
    		Log.d(LogUtil.TAG, "Location changed " + location.getProvider()); //$NON-NLS-1$
    	}
    	if(location != null && OsmandSettings.isSavingTrackToGpx(this)){
			savingTrackHelper.insertData(location.getLatitude(), location.getLongitude(), 
					location.getAltitude(), location.getSpeed(), location.getTime(), settings);
		}
    	registerUnregisterSensor(location);
    	updateSpeedBearing(location);
    	
    	locationLayer.setLastKnownLocation(location);
    	if(routingHelper.isFollowingMode()){
    		routingHelper.setCurrentLocation(location);
    	}
    	if (location != null) {
			if (isMapLinkedToLocation()) {
				if(OsmandSettings.isAutoZoomEnabled(settings) && location.hasSpeed()){
	    			int z = defineZoomFromSpeed(location.getSpeed(), mapView.getZoom());
	    			if(mapView.getZoom() != z && !mapView.mapIsAnimating()){
	    				long now = System.currentTimeMillis();
	    				// prevent ui hysterisis (check time interval for autozoom)
	    				if(Math.abs(mapView.getZoom() - z) > 1 || (lastTimeAutoZooming - now) > 6500){
	    					lastTimeAutoZooming = now;
	    					mapView.setZoom(z);
	    				}
	    			}
	    		}
				if (location.hasBearing() && currentMapRotation == OsmandSettings.ROTATE_MAP_BEARING) {
					mapView.setRotate(-location.getBearing());
				}
				mapView.setLatLon(location.getLatitude(), location.getLongitude());
			} else {
				if(backToLocation.getVisibility() != View.VISIBLE){
					backToLocation.setVisibility(View.VISIBLE);
				}
			}
		} else {
			if(backToLocation.getVisibility() != View.INVISIBLE){
				backToLocation.setVisibility(View.INVISIBLE);
			}
		}
    }
    
    public int defineZoomFromSpeed(float speed, int currentZoom){
    	speed *= 3.6;
    	if(speed < 4){
    		return currentZoom;
    	} else if(speed < 33){
    		// less than 33 - show 17 
    		return 17;
    	} else if(speed < 53){
    		return 16;
    	} else if(speed < 83){
    		return 15;
    	}
    	// more than 80 - show 14 (it is slow)
    	return 14;
    }

	public void navigateToPoint(LatLon point){
		if(point != null){
			OsmandSettings.setPointToNavigate(this, point.getLatitude(), point.getLongitude());
		} else {
			OsmandSettings.clearPointToNavigate(settings);
		}
		routingHelper.setFinalAndCurrentLocation(point, null, routingHelper.getCurrentGPXRoute());
		if(point == null){
			routingHelper.setFollowingMode(false);
			stopLogger();
			
			OsmandSettings.setFollowingByRoute(MapActivity.this, false);
		}
		navigationLayer.setPointToNavigate(point);
	}
	
	public Location getLastKnownLocation(){
		return locationLayer.getLastKnownLocation();
	}
	
	public LatLon getMapLocation(){
		return new LatLon(mapView.getLatitude(), mapView.getLongitude());
	}
	
	public LatLon getPointToNavigate(){
		return navigationLayer.getPointToNavigate();
	}
	
	public RoutingHelper getRoutingHelper() {
		return routingHelper;
	}
	
	private boolean isRunningOnEmulator(){
		if (Build.DEVICE.equals("generic")) { //$NON-NLS-1$ 
			return true;
		}  
		return false;
	}
	
	private boolean useOnlyGPS() {
		return (routingHelper != null && routingHelper.isFollowingMode())
				|| (System.currentTimeMillis() - lastTimeGPXLocationFixed) < USE_ONLY_GPS_INTERVAL || isRunningOnEmulator();
	}
    

	// Working with location listeners
	private LocationListener networkListener = new LocationListener(){
		
		@Override
		public void onLocationChanged(Location location) {
			// double check about use only gps
			// that strange situation but it could happen?
			if(!Algoritms.objectEquals(currentLocationProvider, LocationManager.GPS_PROVIDER) &&  
					!useOnlyGPS()){
				setLocation(location);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			if(!useOnlyGPS()){
				setLocation(null);
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if(LocationProvider.OUT_OF_SERVICE == status && !useOnlyGPS()){
				setLocation(null);
			}
		}
	};
	private LocationListener gpsListener = new LocationListener(){
		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				lastTimeGPXLocationFixed = location.getTime();
			}
			setLocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			setLocation(null);
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
			LocationProvider prov = service.getProvider(LocationManager.NETWORK_PROVIDER);
			// do not change provider for temporarily unavailable (possible bug for htc hero 2.1 ?)
			if (LocationProvider.OUT_OF_SERVICE == status /*|| LocationProvider.TEMPORARILY_UNAVAILABLE == status*/) {
				if(LocationProvider.OUT_OF_SERVICE == status){
					setLocation(null);
				}
				// do not use it in routing
				if (!useOnlyGPS() &&  
						service.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					if (!Algoritms.objectEquals(currentLocationProvider, LocationManager.NETWORK_PROVIDER)) {
						currentLocationProvider = LocationManager.NETWORK_PROVIDER;
						service.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_TIMEOUT_REQUEST, GPS_DIST_REQUEST, this);
						providerSupportsBearing = prov == null ? false : prov.supportsBearing() && !isRunningOnEmulator();
						providerSupportsSpeed = prov == null ? false : prov.supportsSpeed() && !isRunningOnEmulator();
					}
				}
			} else if (LocationProvider.AVAILABLE == status) {
				if (!Algoritms.objectEquals(currentLocationProvider, LocationManager.GPS_PROVIDER)) {
					currentLocationProvider = LocationManager.GPS_PROVIDER;
					service.removeUpdates(networkListener);
					prov = service.getProvider(LocationManager.GPS_PROVIDER);
					providerSupportsBearing = prov == null ? false : prov.supportsBearing() && !isRunningOnEmulator();
					providerSupportsSpeed = prov == null ? false : prov.supportsSpeed() && !isRunningOnEmulator();
				}
			}

		}
	};
	
	

	
	@Override
	protected void onPause() {
		super.onPause();
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		service.removeUpdates(gpsListener);
		service.removeUpdates(networkListener);
		
		SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorMgr.unregisterListener(this);
		sensorRegistered = false;
		currentLocationProvider = null;
		routingHelper.setUiActivity(null);
		
		((OsmandApplication)getApplication()).getDaynightHelper().onMapPause();
		
		OsmandSettings.setLastKnownMapLocation(this, (float) mapView.getLatitude(), (float) mapView.getLongitude());
		AnimateDraggingMapThread animatedThread = mapView.getAnimatedDraggingThread();
		if(animatedThread.isAnimating() && animatedThread.getTargetZoom() != 0){
			OsmandSettings.setMapLocationToShow(this, animatedThread.getTargetLatitude(), animatedThread.getTargetLongitude(), 
					animatedThread.getTargetZoom());
		}
		
		OsmandSettings.setLastKnownMapZoom(this, mapView.getZoom());
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
		OsmandSettings.setMapActivityEnabled(this, false);
		((OsmandApplication)getApplication()).getResourceManager().interruptRendering();
		((OsmandApplication)getApplication()).getResourceManager().setBusyIndicator(null);
	}
	
	private void updateApplicationModeSettings(){
		currentMapRotation = OsmandSettings.getRotateMap(settings);
		currentShowingAngle = OsmandSettings.isShowingViewAngle(settings);
		if(currentMapRotation == OsmandSettings.ROTATE_MAP_NONE){
			mapView.setRotate(0);
		}
		if(!currentShowingAngle){
			locationLayer.setHeading(null);
		}
		locationLayer.setAppMode(OsmandSettings.getApplicationMode(settings));
		routingHelper.setAppMode(OsmandSettings.getApplicationMode(settings));
		mapView.setMapPosition(OsmandSettings.getPositionOnMap(settings));
		registerUnregisterSensor(getLastKnownLocation());
		updateLayers();
	}
	
	private void updateLayers(){
		if(mapView.getLayers().contains(transportStopsLayer) != OsmandSettings.isShowingTransportOverMap(settings)){
			if(OsmandSettings.isShowingTransportOverMap(settings)){
				mapView.addLayer(transportStopsLayer, 5);
			} else {
				mapView.removeLayer(transportStopsLayer);
			}
		}
		if(mapView.getLayers().contains(osmBugsLayer) != OsmandSettings.isShowingOsmBugs(settings)){
			if(OsmandSettings.isShowingOsmBugs(settings)){
				mapView.addLayer(osmBugsLayer, 2);
			} else {
				mapView.removeLayer(osmBugsLayer);
			}
		}

		if(mapView.getLayers().contains(poiMapLayer) != OsmandSettings.isShowingPoiOverMap(settings)){
			if(OsmandSettings.isShowingPoiOverMap(settings)){
				mapView.addLayer(poiMapLayer, 3);
			} else {
				mapView.removeLayer(poiMapLayer);
			}
		}
		
		if(mapView.getLayers().contains(favoritesLayer) != OsmandSettings.isShowingFavorites(settings)){
			if(OsmandSettings.isShowingFavorites(settings)){
				mapView.addLayer(favoritesLayer, 4);
			} else {
				mapView.removeLayer(favoritesLayer);
			}
		}
		if(mapView.getLayers().contains(aghTrafficLayer) != OsmandSettings.isShowingAghTraffic(settings)){
			if(OsmandSettings.isShowingAghTraffic(settings)){
				mapView.addLayer(aghTrafficLayer, 0.8f);
			} else {
				mapView.removeLayer(aghTrafficLayer);
			}
		}
		trafficLayer.setVisible(OsmandSettings.isShowingYandexTraffic(settings));
	}
	
	private void updateMapSource(){
		boolean vectorData = OsmandSettings.isUsingMapVectorData(settings);
		OsmandApplication app = ((OsmandApplication)getApplication());
		ResourceManager rm = app.getResourceManager();
		if(vectorData && !app.isApplicationInitializing()){
			if(rm.getRenderer().isEmpty()){
				Toast.makeText(MapActivity.this, getString(R.string.no_vector_map_loaded), Toast.LENGTH_LONG).show();
				vectorData = false;
			}
		}
		ITileSource newSource = OsmandSettings.getMapTileSource(settings);
		if(mapView.getMap() instanceof SQLiteTileSource){
			((SQLiteTileSource)mapView.getMap()).closeDB();
		}
		rm.updateMapSource(vectorData, newSource);
		
		mapView.setMap(vectorData ? null : newSource);
		ZoomControls zoomControls = (ZoomControls) findViewById(R.id.ZoomControls);
		zoomControls.setIsZoomInEnabled(mapView.getZoom() + 1 < mapView.getMaximumShownMapZoom());
		zoomControls.setIsZoomOutEnabled(mapView.getZoom() + 1 > mapView.getMinimumShownMapZoom());
		rendererLayer.setVisible(vectorData);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(OsmandSettings.getMapOrientation(settings) != getRequestedOrientation()){
			setRequestedOrientation(OsmandSettings.getMapOrientation(settings));
			// can't return from this method we are not sure if activity will be recreated or not
		}
		currentScreenOrientation = getWindow().getWindowManager().getDefaultDisplay().getOrientation();
		
		boolean showTiles = !OsmandSettings.isUsingMapVectorData(settings);
		ITileSource source = showTiles ? OsmandSettings.getMapTileSource(settings) : null;
		if (showTiles != !rendererLayer.isVisible() || !Algoritms.objectEquals(mapView.getMap(), source)) {
			updateMapSource();
		}
		
		updateApplicationModeSettings();
		

		poiMapLayer.setFilter(OsmandSettings.getPoiFilterForMap(this, (OsmandApplication) getApplication()));
		backToLocation.setVisibility(View.INVISIBLE);
		
		routingHelper.setUiActivity(this);

		
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		try {
			service.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_TIMEOUT_REQUEST, GPS_DIST_REQUEST, gpsListener);
			currentLocationProvider = LocationManager.GPS_PROVIDER;
		} catch (IllegalArgumentException e) {
			Log.d(LogUtil.TAG, "GPS location provider not available"); //$NON-NLS-1$
		}
		if(!useOnlyGPS()){
			// try to always  ask for network provide : it is faster way to find location
			try {
				service.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_TIMEOUT_REQUEST, GPS_DIST_REQUEST, networkListener);
				currentLocationProvider = LocationManager.NETWORK_PROVIDER;
			} catch(IllegalArgumentException e) {
				Log.d(LogUtil.TAG, "Network location provider not available"); //$NON-NLS-1$
			}
		}
		
		LocationProvider  prov = service.getProvider(currentLocationProvider); 
		providerSupportsBearing = prov == null ? false : prov.supportsBearing() && !isRunningOnEmulator();
		providerSupportsSpeed = prov == null ? false : prov.supportsSpeed() && !isRunningOnEmulator();
		
		if(settings != null && settings.contains(OsmandSettings.LAST_KNOWN_MAP_LAT)){
			LatLon l = OsmandSettings.getLastKnownMapLocation(settings);
			mapView.setLatLon(l.getLatitude(), l.getLongitude());
			mapView.setZoom(OsmandSettings.getLastKnownMapZoom(settings));
		}
		
		
		if (wakeLock == null) {
			PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
			wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "net.osmand.map"); //$NON-NLS-1$
			wakeLock.acquire();
		}
		
		OsmandSettings.setMapActivityEnabled(this, true);
		checkExternalStorage();
		showAndHideMapPosition();
		
		LatLon latLon = OsmandSettings.getAndClearMapLocationToShow(settings);
		LatLon cur = new LatLon(mapView.getLatitude(), mapView.getLongitude());
		if (latLon != null && !latLon.equals(cur)) {
			mapView.getAnimatedDraggingThread().startMoving(cur.getLatitude(), cur.getLongitude(), latLon.getLatitude(),
					latLon.getLongitude(), mapView.getZoom(), OsmandSettings.getMapZoomToShow(settings), mapView.getSourceTileSize(),
					mapView.getRotate(), true);
		}
		
		
		View progress = findViewById(R.id.ProgressBar);
		if(progress != null){
			((OsmandApplication) getApplication()).getResourceManager().setBusyIndicator(new BusyIndicator(this, progress));
		}
		
		((OsmandApplication)getApplication()).getDaynightHelper().onMapResume();
	}
	
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			contextMenuPoint(mapView.getLatitude(), mapView.getLongitude());
	    	return true;
		}
		return false;
	}
	
	public void checkExternalStorage(){
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state)){
			// ok
		} else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			Toast.makeText(this, R.string.sd_mounted_ro, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, R.string.sd_unmounted, Toast.LENGTH_LONG).show();
		}
	}
	
	
	public void showAndHideMapPosition(){
		mapView.setShowMapPosition(true);
		if(mapPositionHandler == null){
			mapPositionHandler = new Handler();
		}
		Message msg = Message.obtain(mapPositionHandler, new Runnable(){
			@Override
			public void run() {
				if(mapView.isShowMapPosition()){
					mapView.setShowMapPosition(false);
					mapView.refreshMap();
				}
			}
			
		});
		msg.what = 7;
		mapPositionHandler.removeMessages(7);
		mapPositionHandler.sendMessageDelayed(msg, 2500);
		
	}
	
	

	@Override
	public void locationChanged(double newLatitude, double newLongitude, Object source) {
		// when user start dragging 
		if(locationLayer.getLastKnownLocation() != null){
			if(isMapLinkedToLocation()){
				OsmandSettings.setSyncMapToGpsLocation(MapActivity.this, false);
			}
			if (backToLocation.getVisibility() != View.VISIBLE) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						backToLocation.setVisibility(View.VISIBLE);
					}
				});
			}
		}
	}
	
	public void setMapLocation(double lat, double lon){
		mapView.setLatLon(lat, lon);
		locationChanged(lat, lon, this);
	}
	
	public OsmandMapTileView getMapView() {
		return mapView;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		// Attention : sensor produces a lot of events & can hang the system
		float val = event.values[0];
		if(currentScreenOrientation == 1){
			val += 90;
		}
		if (currentMapRotation == OsmandSettings.ROTATE_MAP_COMPASS) {
			mapView.setRotate(-val);
		}
		if(currentShowingAngle){
			if(locationLayer.getHeading() == null || Math.abs(locationLayer.getHeading() - val) > 10){
				locationLayer.setHeading(val);
			}
		}
		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
		
		MenuItem item = menu.findItem(R.id.map_enable_ad_hoc);
		if(application.adHocModule.isProcessRunning()) {
			item.setChecked(true);
		} else {
			item.setChecked(false);
		}
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean val = super.onPrepareOptionsMenu(menu);
		MenuItem navigateToPointMenu = menu.findItem(R.id.map_navigate_to_point);
		if (navigateToPointMenu != null) {
			navigateToPointMenu.setTitle(routingHelper.isRouteCalculated() ? R.string.stop_routing : R.string.stop_navigation);
			if (OsmandSettings.getPointToNavigate(settings) != null) {
				navigateToPointMenu.setVisible(true);
			} else {
				navigateToPointMenu.setVisible(false);
			}
		}
		MenuItem muteMenu = menu.findItem(R.id.map_mute); 
		if(muteMenu != null){
			muteMenu.setTitle(routingHelper.getVoiceRouter().isMute() ? R.string.menu_mute_on : R.string.menu_mute_off);
			if (routingHelper.getFinalLocation() != null && routingHelper.isFollowingMode()) {
				muteMenu.setVisible(true);
			} else {
				muteMenu.setVisible(false);
			}
		}
		
		return val;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.map_show_settings) {
    		final Intent settings = new Intent(MapActivity.this, SettingsActivity.class);
			startActivity(settings);
    		return true;
		} else if (item.getItemId() == R.id.map_menu_send_points) {
			// for test only
			LocationBuffer.INSTANCE.sendLocations();
			return true;
		} else if (item.getItemId() == R.id.map_menu_clear_points) {
			// for test only
			LocationBuffer.INSTANCE.getAndClearLocations();
			return true;
		} else if (item.getItemId() == R.id.map_where_am_i) {
			backToLocationImpl();
        	return true;
		} else if (item.getItemId() == R.id.map_show_gps_status) {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName(GPS_STATUS_COMPONENT, GPS_STATUS_ACTIVITY));
			ResolveInfo resolved = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
			if(resolved != null){
				startActivity(intent);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getString(R.string.gps_status_app_not_found));
				builder.setPositiveButton(getString(R.string.default_buttons_yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:" + GPS_STATUS_COMPONENT));
								try {
									startActivity(intent);
								} catch (ActivityNotFoundException e) {
								}
							}
						});
				builder.setNegativeButton(getString(R.string.default_buttons_no), null);
				builder.show();
			}
			return true;
//		} else if (item.getItemId() == R.id.map_mark_point) {
//			contextMenuPoint(mapView.getLatitude(), mapView.getLongitude());
//			return true;
		} else if (item.getItemId() == R.id.map_get_directions) {
			getDirections(mapView.getLatitude(), mapView.getLongitude(), true);
			return true;
		} else if (item.getItemId() == R.id.map_layers) {
			openLayerSelectionDialog();
			return true;
		} else if (item.getItemId() == R.id.map_specify_point) {
			openChangeLocationDialog();
			return true;
		} else if (item.getItemId() == R.id.map_mute) {
			routingHelper.getVoiceRouter().setMute(!routingHelper.getVoiceRouter().isMute());
			return true;
    	}  else if (item.getItemId() == R.id.map_navigate_to_point) {
    		if(navigationLayer.getPointToNavigate() != null){
    			if(routingHelper.isRouteCalculated()){
    				routingHelper.setFinalAndCurrentLocation(null, null);
    				OsmandSettings.setFollowingByRoute(MapActivity.this, false);
    				routingHelper.setFollowingMode(false);
    				stopLogger();
    			} else {
    				navigateToPoint(null);
    			}
    		} else {
    			navigateToPoint(new LatLon(mapView.getLatitude(), mapView.getLongitude()));
    		}
			mapView.refreshMap();
    	} else if (item.getItemId() == R.id.map_gpx_routing) {
			useGPXFileLayer(true, navigationLayer.getPointToNavigate());
			return true;
    	} else if (item.getItemId() == R.id.map_show_point_options) {
			contextMenuPoint(mapView.getLatitude(), mapView.getLongitude());
    		return true;
    	} else if (item.getItemId() == R.id.map_enable_ad_hoc) {
    		if(item.isChecked()) {
    			stopAdHoc();
    			item.setChecked(false);
    		} else {
    			startAdHoc();
    			item.setChecked(true);
    		}
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    private ApplicationMode getAppMode(ToggleButton[] buttons){
    	for(int i=0; i<buttons.length; i++){
    		if(buttons[i] != null && buttons[i].isChecked() && i < ApplicationMode.values().length){
    			return ApplicationMode.values()[i];
    		}
    	}
    	return OsmandSettings.getApplicationMode(settings);
    }
    
    
    protected void getDirections(final double lat, final double lon, boolean followEnabled){
    	if(navigationLayer.getPointToNavigate() == null){
			Toast.makeText(this, R.string.mark_final_location_first, Toast.LENGTH_LONG).show();
			return;
		}
    	Builder builder = new AlertDialog.Builder(this);
    	
    	View view = getLayoutInflater().inflate(R.layout.calculate_route, null);
    	final ToggleButton[] buttons = new ToggleButton[ApplicationMode.values().length];
    	buttons[ApplicationMode.CAR.ordinal()] = (ToggleButton) view.findViewById(R.id.CarButton);
    	buttons[ApplicationMode.BICYCLE.ordinal()] = (ToggleButton) view.findViewById(R.id.BicycleButton);
    	buttons[ApplicationMode.PEDESTRIAN.ordinal()] = (ToggleButton) view.findViewById(R.id.PedestrianButton);
    	ApplicationMode appMode = OsmandSettings.getApplicationMode(settings);
    	for(int i=0; i< buttons.length; i++){
    		if(buttons[i] != null){
    			final int ind = i;
    			ToggleButton b = buttons[i];
    			b.setChecked(appMode == ApplicationMode.values()[i]);
    			b.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(isChecked){
							for (int j = 0; j < buttons.length; j++) {
								if (buttons[j] != null) {
									if(buttons[j].isChecked() != (ind == j)){
										buttons[j].setChecked(ind == j);
									}
								}
							}
						} else {
							// revert state
							boolean revert = true;
							for (int j = 0; j < buttons.length; j++) {
								if (buttons[j] != null) {
									if(buttons[j].isChecked()){
										revert = false;
										break;
									}
								}
							}
							if (revert){ 
								buttons[ind].setChecked(true);
							}
						}
					}
    			});
    		}
    	}
    	
    	DialogInterface.OnClickListener onlyShowCall = new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ApplicationMode mode = getAppMode(buttons);
				Location map = new Location("map"); //$NON-NLS-1$
				map.setLatitude(lat);
				map.setLongitude(lon);
				routingHelper.setAppMode(mode);
				OsmandSettings.setFollowingByRoute(MapActivity.this, false);
				routingHelper.setFollowingMode(false);
				stopLogger();
				routingHelper.setFinalAndCurrentLocation(navigationLayer.getPointToNavigate(), map);
			}
    	};
    	
    	DialogInterface.OnClickListener followCall = new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ApplicationMode mode = getAppMode(buttons);
				// change global settings
				ApplicationMode old = OsmandSettings.getApplicationMode(settings);
				if (old != mode) {
					Editor edit = getSharedPreferences(OsmandSettings.SHARED_PREFERENCES_NAME, MODE_WORLD_WRITEABLE).edit();
					edit.putString(OsmandSettings.APPLICATION_MODE, mode.name());
					SettingsActivity.setAppMode(mode, edit, (OsmandApplication) getApplication(), old);
					edit.commit();
					updateApplicationModeSettings();	
					mapView.refreshMap();
				}
				
				Location location = locationLayer.getLastKnownLocation();
				if(location == null){
					location = new Location("map"); //$NON-NLS-1$
					location.setLatitude(lat);
					location.setLongitude(lon);
				} 
				routingHelper.setAppMode(mode);
				OsmandSettings.setFollowingByRoute(MapActivity.this, true);
				routingHelper.setFollowingMode(true);
				startLogger();
				routingHelper.setFinalAndCurrentLocation(navigationLayer.getPointToNavigate(), location);
				
				((OsmandApplication) getApplication()).showDialogInitializingCommandPlayer(MapActivity.this);
				
			}
    	};
    	DialogInterface.OnClickListener showRoute = new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(MapActivity.this, ShowRouteInfoActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		};
    	
    	builder.setView(view);
    	if (followEnabled) {
    		builder.setTitle(R.string.follow_route);
			builder.setPositiveButton(R.string.follow, followCall);
			if (routingHelper.isRouterEnabled() && routingHelper.isRouteCalculated()) {
				builder.setNeutralButton(R.string.route_about, showRoute);
			}
			builder.setNegativeButton(R.string.only_show, onlyShowCall);
		} else {
			builder.setTitle(R.string.show_route);
			view.findViewById(R.id.TextView).setVisibility(View.GONE);
    		builder.setPositiveButton(R.string.show_route, onlyShowCall);
    		builder.setNegativeButton(R.string.default_buttons_cancel, null);
    	}
    	builder.show();
    }
    
    
    protected void reloadTile(final int zoom, final double latitude, final double longitude){
    	Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(R.string.context_menu_item_update_map_confirm);
    	builder.setNegativeButton(R.string.default_buttons_cancel, null);
    	builder.setPositiveButton(R.string.context_menu_item_update_map, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Rect pixRect = new Rect(0, 0, mapView.getWidth(), mapView.getHeight());
		    	RectF tilesRect = new RectF();
		    	mapView.calculateTileRectangle(pixRect, mapView.getCenterPointX(), mapView.getCenterPointY(), 
		    			mapView.getXTile(), mapView.getYTile(), tilesRect);
		    	int left = (int) FloatMath.floor(tilesRect.left);
				int top = (int) FloatMath.floor(tilesRect.top);
				int width = (int) (FloatMath.ceil(tilesRect.right) - left);
				int height = (int) (FloatMath.ceil(tilesRect.bottom) - top);
				for (int i = 0; i <width; i++) {
					for (int j = 0; j< height; j++) {
						((OsmandApplication)getApplication()).getResourceManager().
								clearTileImageForMap(null, mapView.getMap(), i + left, j + top, zoom);	
					}
				}
				
				
				mapView.refreshMap();
			}
    	});
    	builder.setNeutralButton(R.string.context_menu_item_update_poi, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				updatePoiDb(zoom, latitude, longitude);
				
			}
    	});
		builder.create().show();
    }
    
    protected void parseLaunchIntentLocation(){
    	Intent intent = getIntent();
    	if(intent != null && intent.getData() != null){
    		Uri data = intent.getData();
    		if("http".equalsIgnoreCase(data.getScheme()) && "download.osmand.net".equals(data.getHost()) &&
    				"/go".equals( data.getPath())) {
    			String lat = data.getQueryParameter("lat");
    			String lon = data.getQueryParameter("lon");
				if (lat != null && lon != null) {
					try {
						double lt = Double.parseDouble(lat);
						double ln = Double.parseDouble(lon);
						SharedPreferences prefs = OsmandSettings.getSharedPreferences(this);
						Editor edit = prefs.edit();
						edit.putFloat(OsmandSettings.LAST_KNOWN_MAP_LAT, (float) lt);
						edit.putFloat(OsmandSettings.LAST_KNOWN_MAP_LON, (float) ln);
						String zoom = data.getQueryParameter("z");
						if(zoom != null){
							edit.putInt(OsmandSettings.LAST_KNOWN_MAP_ZOOM, Integer.parseInt(zoom));
						}
						edit.commit();
					} catch (NumberFormatException e) {
					}
				}
    		}
    	}
    }
    
    
    protected void showToast(final String msg){
    	runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Toast.makeText(MapActivity.this, msg, Toast.LENGTH_LONG).show();
			}
    	});
    }
    
    protected void updatePoiDb(int zoom, double latitude, double longitude){
    	if(zoom < 15){
    		Toast.makeText(this, getString(R.string.update_poi_is_not_available_for_zoom), Toast.LENGTH_SHORT).show();
    		return;
    	}
    	final List<AmenityIndexRepository> repos = ((OsmandApplication) getApplication()).
    								getResourceManager().searchAmenityRepositories(latitude, longitude);
    	if(repos.isEmpty()){
    		Toast.makeText(this, getString(R.string.update_poi_no_offline_poi_index), Toast.LENGTH_SHORT).show();
    		return;
    	}
    	Rect pixRect = new Rect(-mapView.getWidth()/2, -mapView.getHeight()/2, 3*mapView.getWidth()/2, 3*mapView.getHeight()/2);
    	RectF tileRect = new RectF();
    	mapView.calculateTileRectangle(pixRect, mapView.getCenterPointX(), mapView.getCenterPointY(), 
    			mapView.getXTile(), mapView.getYTile(), tileRect);
    	final double leftLon = MapUtils.getLongitudeFromTile(zoom, tileRect.left); 
    	final double topLat = MapUtils.getLatitudeFromTile(zoom, tileRect.top);
		final double rightLon = MapUtils.getLongitudeFromTile(zoom, tileRect.right);
		final double bottomLat = MapUtils.getLatitudeFromTile(zoom, tileRect.bottom);
    	
    	progressDlg = ProgressDialog.show(this, getString(R.string.loading), getString(R.string.loading_data));
    	new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					List<Amenity> amenities = new ArrayList<Amenity>();
					boolean loadingPOIs = AmenityIndexRepositoryOdb.loadingPOIs(amenities, leftLon, topLat, rightLon, bottomLat);
					if(!loadingPOIs){
						showToast(getString(R.string.update_poi_error_loading));
					} else {
						for(AmenityIndexRepository r  : repos){
							if(r instanceof AmenityIndexRepositoryOdb){
								((AmenityIndexRepositoryOdb) r).updateAmenities(amenities, leftLon, topLat, rightLon, bottomLat);
							}
						}
						showToast(MessageFormat.format(getString(R.string.update_poi_success), amenities.size()));
						mapView.refreshMap();
					}
				} catch(Exception e) {
					Log.e(LogUtil.TAG, "Error updating local data", e); //$NON-NLS-1$
					showToast(getString(R.string.update_poi_error_local));
				} finally {
					if(progressDlg !=null){
						progressDlg.dismiss();
						progressDlg = null;
					}
				}
			}
    	}, "LoadingPOI").start(); //$NON-NLS-1$
    	
    }
    
	private void openLayerSelectionDialog(){
		List<String> layersList = new ArrayList<String>();
		layersList.add(getString(R.string.layer_map));
		layersList.add(getString(R.string.layer_poi));
		layersList.add(getString(R.string.layer_transport));
		layersList.add(getString(R.string.layer_osm_bugs));
		layersList.add(getString(R.string.layer_favorites));
		layersList.add(getString(R.string.layer_gpx_layer));
		layersList.add(getString(R.string.layer_agh_traffic));
		
		final int routeInfoInd = routeInfoLayer.couldBeVisible() ? layersList.size() : -1;
		if(routeInfoLayer.couldBeVisible()){
			layersList.add(getString(R.string.layer_route));
		}
		final int transportRouteInfoInd = !TransportRouteHelper.getInstance().routeIsCalculated() ? - 1 : layersList.size(); 
		if(transportRouteInfoInd > -1){
			layersList.add(getString(R.string.layer_transport_route));
		}
		
		final boolean[] selected = new boolean[layersList.size()];
		Arrays.fill(selected, true);
		selected[1] = OsmandSettings.isShowingPoiOverMap(settings);
		selected[2] = OsmandSettings.isShowingTransportOverMap(settings);
		selected[3] = OsmandSettings.isShowingOsmBugs(settings);
		selected[4] = OsmandSettings.isShowingFavorites(settings);
		selected[5] = gpxLayer.isVisible();
		selected[6] = OsmandSettings.isShowingAghTraffic(settings);
		if(routeInfoInd != -1){
			selected[routeInfoInd] = routeInfoLayer.isUserDefinedVisible(); 
		}
		if(transportRouteInfoInd != -1){
			selected[transportRouteInfoInd] = transportInfoLayer.isVisible(); 
		}
		
		Builder builder = new AlertDialog.Builder(this);
		builder.setMultiChoiceItems(layersList.toArray(new String[layersList.size()]), selected, new DialogInterface.OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int item, boolean isChecked) {
				if (item == 0) {
					dialog.dismiss();
					selectMapLayer();
				} else if(item == 1){
					if(isChecked){
						selectPOIFilterLayer();
					}
					OsmandSettings.setShowPoiOverMap(MapActivity.this, isChecked);
				} else if(item == 2){
					OsmandSettings.setShowTransortOverMap(MapActivity.this, isChecked);
				} else if(item == 3){
					OsmandSettings.setShowingOsmBugs(MapActivity.this, isChecked);
				} else if(item == 4){
					OsmandSettings.setShowingFavorites(MapActivity.this, isChecked);
				} else if(item == 5){
					if(gpxLayer.isVisible()){
						gpxLayer.clearCurrentGPX();
						getFavoritesHelper().setFavoritePointsFromGPXFile(null);
					} else {
						dialog.dismiss();
						useGPXFileLayer(false, null);
					}
				} else if (item == 6) {
					OsmandSettings.setShowingAghTraffic(MapActivity.this, isChecked);
				} else if(item == routeInfoInd){
					routeInfoLayer.setVisible(isChecked);
				} else if(item == transportRouteInfoInd){
					transportInfoLayer.setVisible(isChecked);
				} 
				updateLayers();
				mapView.refreshMap();
			}
		});
		builder.show();
	}
	
	private void useGPXFileLayer(final boolean useRouting, final LatLon endForRouting) {
		final List<String> list = new ArrayList<String>();
		final File dir = OsmandSettings.extendOsmandPath(getApplicationContext(), ResourceManager.APP_DIR + SavingTrackHelper.TRACKS_PATH);
		if (dir != null && dir.canRead()) {
			File[] files = dir.listFiles();
			if (files != null) {
				Arrays.sort(files, new Comparator<File>() {
					@Override
					public int compare(File object1, File object2) {
						if (object1.lastModified() > object2.lastModified()) {
							return -1;
						} else if (object1.lastModified() == object2.lastModified()) {
							return 0;
						}
						return 1;
					}

				});

				for (File f : files) {
					if (f.getName().endsWith(".gpx")) { //$NON-NLS-1$
						list.add(f.getName());
					}
				}
			}
		}
		
		if(list.isEmpty()){
			Toast.makeText(this, R.string.gpx_files_not_found, Toast.LENGTH_LONG).show();
		} else {
			Builder builder = new AlertDialog.Builder(this);
			builder.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					final ProgressDialog dlg = ProgressDialog.show(MapActivity.this, getString(R.string.loading),
							getString(R.string.loading_data));
					final File f = new File(dir, list.get(which));
					new Thread(new Runnable() {
						@Override
						public void run() {
							Looper.prepare();
							final GPXFileResult res = GPXUtilities.loadGPXFile(MapActivity.this, f);
							if (res.error != null) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(MapActivity.this, res.error, Toast.LENGTH_LONG).show();
									}
								});

							}
							dlg.dismiss();
							if(useRouting){
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										useGPXRouting(endForRouting, res);
									}
								});
							} else {
								OsmandSettings.setShowingFavorites(MapActivity.this, true);
								List<FavouritePoint> pts = new ArrayList<FavouritePoint>();
								for(WptPt p : res.wayPoints){
									FavouritePoint pt = new FavouritePoint();
									pt.setLatitude(p.lat);
									pt.setLongitude(p.lon);
									pt.setName(p.name);
									pts.add(pt);
								}
								getFavoritesHelper().setFavoritePointsFromGPXFile(pts);
								gpxLayer.setTracks(res.locations);
							}
							mapView.refreshMap();

						}

						
					}, "Loading gpx").start(); //$NON-NLS-1$
				}

			});
			builder.show();
		}
		
	}
	
	public FavouritesDbHelper getFavoritesHelper() {
		return ((OsmandApplication)getApplication()).getFavorites();
	}
	
	private void useGPXRouting(final LatLon endForRouting, final GPXFileResult res) {
		Builder builder = new AlertDialog.Builder(this);
		builder.setItems(new String[]{getString(R.string.gpx_direct_route), getString(R.string.gpx_reverse_route)}, 
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				boolean reverse = which == 1;
				ArrayList<List<Location>> locations = res.locations;
				List<Location> l = new ArrayList<Location>();
				for(List<Location> s : locations){
					l.addAll(s);
				}
				if(reverse){
					Collections.reverse(l);
				}
				Location startForRouting = locationLayer.getLastKnownLocation();
				if(startForRouting == null && !l.isEmpty()){
					startForRouting = l.get(0);
				}
				LatLon endPoint = endForRouting;
				if(/*endForRouting == null && */!l.isEmpty()){
					LatLon point = new LatLon(l.get(l.size() - 1).getLatitude(), l.get(l.size() - 1).getLongitude());
					OsmandSettings.setPointToNavigate(MapActivity.this, point.getLatitude(), point.getLongitude());
					endPoint = point;
					navigationLayer.setPointToNavigate(point);
				}
				if(endForRouting != null){
					OsmandSettings.setFollowingByRoute(MapActivity.this, true);
					routingHelper.setFollowingMode(true);
					startLogger();					
					routingHelper.setFinalAndCurrentLocation(endPoint, startForRouting, l);
					((OsmandApplication)getApplication()).showDialogInitializingCommandPlayer(MapActivity.this);
				}
				
			}
		
		});
		builder.show();
	}
		
	private void selectPOIFilterLayer(){
		final List<PoiFilter> userDefined = new ArrayList<PoiFilter>();
		List<String> list = new ArrayList<String>();
		list.add(getString(R.string.any_poi));
		
		final PoiFiltersHelper poiFilters = ((OsmandApplication)getApplication()).getPoiFilters();
		for (PoiFilter f : poiFilters.getUserDefinedPoiFilters()) {
			userDefined.add(f);
			list.add(f.getName());
		}
		for(AmenityType t : AmenityType.values()){
			list.add(OsmAndFormatter.toPublicString(t, this));
		}
		Builder builder = new AlertDialog.Builder(this);
		builder.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String filterId;
				if (which == 0) {
					filterId = PoiFiltersHelper.getOsmDefinedFilterId(null);
				} else if (which <= userDefined.size()) {
					filterId = userDefined.get(which - 1).getFilterId();
				} else {
					filterId = PoiFiltersHelper.getOsmDefinedFilterId(AmenityType.values()[which - userDefined.size() - 1]);
				}
				if(filterId.equals(PoiFilter.CUSTOM_FILTER_ID)){
					Intent newIntent = new Intent(MapActivity.this, EditPOIFilterActivity.class);
					newIntent.putExtra(EditPOIFilterActivity.AMENITY_FILTER, filterId);
					newIntent.putExtra(EditPOIFilterActivity.SEARCH_LAT, mapView.getLatitude());
					newIntent.putExtra(EditPOIFilterActivity.SEARCH_LON, mapView.getLongitude());
					startActivity(newIntent);
				} else {
					OsmandSettings.setPoiFilterForMap(MapActivity.this, filterId);
					poiMapLayer.setFilter(poiFilters.getFilterById(filterId));
					mapView.refreshMap();
				}
			}
			
		});
		builder.show();
	}
	
	private void selectMapLayer(){
		Map<String, String> entriesMap = SettingsActivity.getTileSourceEntries(this);
		Builder builder = new AlertDialog.Builder(this);
		final ArrayList<String> keys = new ArrayList<String>(entriesMap.keySet());
		String[] items = new String[entriesMap.size() + 1];
		items[0] = getString(R.string.vector_data);
		int i = 1;
		for(String it : entriesMap.values()){
			items[i++] = it;
		}
		builder.setItems(items, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Editor edit = OsmandSettings.getWriteableEditor(MapActivity.this);
				if(which == 0){
					MapRenderRepositories r = ((OsmandApplication)getApplication()).getResourceManager().getRenderer();
					if(r.isEmpty()){
						Toast.makeText(MapActivity.this, getString(R.string.no_vector_map_loaded), Toast.LENGTH_LONG).show();
						return;
					} else {
						edit.putBoolean(OsmandSettings.MAP_VECTOR_DATA, true);
					}
				} else {
					edit.putBoolean(OsmandSettings.MAP_VECTOR_DATA, false);
					edit.putString(OsmandSettings.MAP_TILE_SOURCES, keys.get(which - 1));
				}
				edit.commit();
				updateMapSource();
			}
			
		});
		builder.show();
	}

    
	public void contextMenuPoint(final double latitude, final double longitude){
		contextMenuPoint(latitude, longitude, null, null);
	}
	
    public void contextMenuPoint(final double latitude, final double longitude, List<String> additionalItems, 
    		final DialogInterface.OnClickListener additionalActions){
    	Builder builder = new AlertDialog.Builder(this);
    	final int sizeAdditional = additionalActions == null || additionalItems == null ? 0 : additionalItems.size();
    	List<String> actions = new ArrayList<String>();
    	if(sizeAdditional > 0){
    		actions.addAll(additionalItems);
    	}
    	final int[] contextMenuStandardActions = new int[]{
    			R.string.context_menu_item_navigate_point,
    			R.string.context_menu_item_select_for_agh,
    			R.string.context_menu_item_search_poi,
    			R.string.context_menu_item_show_route,
    			R.string.context_menu_item_search_transport,
    			R.string.context_menu_item_add_favorite,
    			R.string.context_menu_item_share_location,
    			R.string.context_menu_item_create_poi,
    			R.string.context_menu_item_add_waypoint,
    			R.string.context_menu_item_open_bug,
    			R.string.context_menu_item_update_map,
    			R.string.context_menu_item_download_map
    	};
    	for(int j = 0; j<contextMenuStandardActions.length; j++){
    		actions.add(getResources().getString(contextMenuStandardActions[j]));
    	}
    	
    	builder.setItems(actions.toArray(new String[actions.size()]), new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which < sizeAdditional){
					additionalActions.onClick(dialog, which);
					return;
				}
				int standardId = contextMenuStandardActions[which - sizeAdditional];
				if(standardId == R.string.context_menu_item_navigate_point){
					navigateToPoint(new LatLon(latitude, longitude));
				} else if (standardId == R.string.context_menu_item_select_for_agh) {
					// for test only
					LocationInfo loc = new LocationInfo();
					loc.setLatitude(latitude);
					loc.setLongitude(longitude);
					loc.setTime(new Date());
					LocationBuffer.INSTANCE.addLocation(loc);
					
				} else if(standardId == R.string.context_menu_item_search_poi){
					Intent intent = new Intent(MapActivity.this, SearchPoiFilterActivity.class);
					intent.putExtra(SearchPoiFilterActivity.SEARCH_LAT, latitude);
					intent.putExtra(SearchPoiFilterActivity.SEARCH_LON, longitude);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				} else if(standardId == R.string.context_menu_item_show_route){
					getDirections(latitude, longitude, false);
				} else if(standardId == R.string.context_menu_item_search_transport){
					Intent intent = new Intent(MapActivity.this, SearchTransportActivity.class);
					intent.putExtra(SearchTransportActivity.LAT_KEY, latitude);
					intent.putExtra(SearchTransportActivity.LON_KEY, longitude);
					startActivity(intent);
				} else if(standardId == R.string.context_menu_item_add_favorite){
					addFavouritePoint(latitude, longitude);
				} else if(standardId == R.string.context_menu_item_share_location){
					shareLocation(latitude, longitude, mapView.getZoom());
				} else if(standardId == R.string.context_menu_item_search_poi){
					EditingPOIActivity activity = new EditingPOIActivity(MapActivity.this, (OsmandApplication) getApplication(), mapView);
					activity.showCreateDialog(latitude, longitude);
				} else if(standardId == R.string.context_menu_item_add_waypoint){
					addWaypoint(latitude, longitude);
				} else if(standardId == R.string.context_menu_item_open_bug){
					osmBugsLayer.openBug(MapActivity.this, getLayoutInflater(), mapView, latitude, longitude);
				} else if(standardId == R.string.context_menu_item_update_map){
					reloadTile(mapView.getZoom(), latitude, longitude);
				} else if(standardId == R.string.context_menu_item_download_map){
					DownloadTilesDialog dlg = new DownloadTilesDialog(MapActivity.this, 
							(OsmandApplication) getApplication(), mapView);
					dlg.openDialog();
				}
			}
    	});
		builder.create().show();
    }
    
    
    protected void addFavouritePoint(final double latitude, final double longitude){
    	final Resources resources = this.getResources();
    	final FavouritePoint p = new FavouritePoint(latitude, longitude, resources.getString(R.string.add_favorite_dialog_default_favourite_name));
    	
    	Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.add_favorite_dialog_top_text);
		final EditText editText = new EditText(this);
		builder.setView(editText);
		builder.setNegativeButton(R.string.default_buttons_cancel, null);
		builder.setNeutralButton(R.string.update_existing, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Builder b = new AlertDialog.Builder(MapActivity.this);
				final FavouritesDbHelper helper = ((OsmandApplication)getApplication()).getFavorites();
				final Collection<FavouritePoint> points = helper.getFavouritePoints();
				final String[] ar = new String[points.size()];
				Iterator<FavouritePoint> it = points.iterator();
				int i=0;
				while(it.hasNext()){
					ar[i++] = it.next().getName();
				}
				b.setItems(ar, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						FavouritePoint fv = helper.getFavoritePointByName(ar[which]);
						if(helper.editFavourite(fv, latitude, longitude)){
							Toast.makeText(MapActivity.this, getString(R.string.fav_points_edited), Toast.LENGTH_SHORT).show();
						}
						mapView.refreshMap();
					}
				});
				if(ar.length == 0){
					Toast.makeText(MapActivity.this, getString(R.string.fav_points_not_exist), Toast.LENGTH_SHORT).show();
					helper.close();
				}  else {
					b.show();
				}
			}
			
		});
		builder.setPositiveButton(R.string.default_buttons_add, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final FavouritesDbHelper helper = ((OsmandApplication)getApplication()).getFavorites();
				p.setName(editText.getText().toString());
				boolean added = helper.addFavourite(p);
				if (added) {
					Toast.makeText(MapActivity.this, MessageFormat.format(getString(R.string.add_favorite_dialog_favourite_added_template), p.getName()), Toast.LENGTH_SHORT)
							.show();
				}
				mapView.refreshMap();
			}
		});
		builder.create().show();
    }

    protected void addWaypoint(final double latitude, final double longitude){
    	
    	Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.add_waypoint_dialog_title);
		final EditText editText = new EditText(this);
		builder.setView(editText);
		builder.setNegativeButton(R.string.default_buttons_cancel, null);
		builder.setPositiveButton(R.string.default_buttons_add, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = editText.getText().toString();
				savingTrackHelper.insertPointData(latitude, longitude, System.currentTimeMillis(), name);
				Toast.makeText(MapActivity.this, MessageFormat.format(getString(R.string.add_waypoint_dialog_added), name), Toast.LENGTH_SHORT)
							.show();
				
			}
		});
		builder.create().show();
    }
    
    protected void shareLocation(final double latitude, final double longitude, int zoom){
    	final String shortOsmUrl = MapUtils.buildShortOsmUrl(latitude, longitude, zoom);
		// final String simpleGeo = "geo:"+((float) latitude)+","+((float)longitude) +"?z="+zoom;
		final String appLink = "http://download.osmand.net/go?lat="+((float) latitude)+"&lon="+((float)longitude) +"&z="+zoom;
		
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(R.string.send_location_way_choose_title);
		builder.setItems(new String[]{
				"Email", "SMS", "Clipboard"
		}, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String sms = MessageFormat.format(getString(R.string.send_location_sms_pattern), shortOsmUrl, appLink);
				String email = MessageFormat.format(getString(R.string.send_location_email_pattern), shortOsmUrl, appLink );
				if(which == 0){
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("vnd.android.cursor.dir/email"); //$NON-NLS-1$
					intent.putExtra(Intent.EXTRA_SUBJECT, "Mine location"); //$NON-NLS-1$
					intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(email));
					intent.setType("text/html");
					startActivity(Intent.createChooser(intent, getString(R.string.send_location)));
				} else if(which == 1){
					Intent sendIntent = new Intent(Intent.ACTION_VIEW);
					sendIntent.putExtra("sms_body", sms); 
					sendIntent.setType("vnd.android-dir/mms-sms");
					startActivity(sendIntent);   
				} else if (which == 2){
					ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					clipboard.setText(sms);
				}
				
			}
		});
    	
    	builder.show();
    }
    
    
	private void openChangeLocationDialog() {
		NavigatePointActivity dlg = new NavigatePointActivity(this);
		dlg.showDialog();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	protected void backToLocationImpl() {
		backToLocation.setVisibility(View.INVISIBLE);
		if(!isMapLinkedToLocation()){
			OsmandSettings.setSyncMapToGpsLocation(MapActivity.this, true);
			if(locationLayer.getLastKnownLocation() != null){
				Location lastKnownLocation = locationLayer.getLastKnownLocation();
				AnimateDraggingMapThread thread = mapView.getAnimatedDraggingThread();
				int fZoom = mapView.getZoom() < 15 ? 15 : mapView.getZoom();
				thread.startMoving(mapView.getLatitude(), mapView.getLongitude(), 
						lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), mapView.getZoom(), fZoom, 
						mapView.getSourceTileSize(), mapView.getRotate(), false);
			}
		}
		if(locationLayer.getLastKnownLocation() == null){
			Toast.makeText(this, R.string.unknown_location, Toast.LENGTH_LONG).show();
		}
	}
	
	public void startLogger() {
		startService(new Intent(MapActivity.this, LocationLogger.class));
	}
	
	public void stopLogger() {
		stopService(new Intent(MapActivity.this, LocationLogger.class));
	}

	public void startAdHoc() {
		new Thread(new Runnable(){
             public void run(){
				if (application.adHocModule.startupCheckPerformed == false) {
		            application.adHocModule.startupCheckPerformed = true;
		            
		            // Check root-permission, files
		            if (!application.adHocModule.coretask.hasRootPermission())
		                Toast.makeText(MapActivity.this, "No root permission. Ad-hoc disabled!", Toast.LENGTH_SHORT).show();
		            
		            // Check if binaries need to be updated
		            if (application.adHocModule.binariesExists() == false) {
		                if (application.adHocModule.coretask.hasRootPermission()) {
		                    application.adHocModule.installFiles();
		                }
		            }
		            // Check if native-library needs to be moved
		            application.adHocModule.renewLibrary();            
		        }
				if(!application.adHocModule.isProcessRunning()) {
					application.adHocModule.startAdHoc();
					application.adHocModule.showStartNotification();
				}
            }
		}).start();
	}
	
	public void stopAdHoc() {
		new Thread(new Runnable() {
			public void run() {
				if(application.adHocModule.isProcessRunning()) {
					application.adHocModule.stopAdHoc();
					application.adHocModule.notificationManager.cancelAll();
				}
			}
		}).start();
	}
}
