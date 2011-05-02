package net.osmand.plus.activities;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.osmand.map.TileSourceManager;
import net.osmand.map.TileSourceManager.TileSourceTemplate;
import net.osmand.plus.NavigationService;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.PoiFiltersHelper;
import net.osmand.plus.ProgressDialogImplementation;
import net.osmand.plus.R;
import net.osmand.plus.ResourceManager;
import net.osmand.plus.SQLiteTileSource;
import net.osmand.plus.OsmandSettings.ApplicationMode;
import net.osmand.plus.OsmandSettings.DayNightMode;
import net.osmand.plus.OsmandSettings.MetricsConstants;
import net.osmand.plus.activities.RouteProvider.RouteService;
import net.osmand.plus.render.BaseOsmandRender;
import net.osmand.plus.render.RendererRegistry;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {
	private final static String VECTOR_MAP = "#VECTOR_MAP"; //$NON-NLS-1$
	
	private class BooleanPreference {
		private final boolean defValue;
		private final String id;
		private CheckBoxPreference pref;

		public BooleanPreference(String id, boolean defValue){
			this.id = id;
			this.defValue = defValue;
		}
		
		public String getId() {
			return id;
		}
		
		public boolean getDefValue() {
			return defValue;
		}
		
		public void setPref(CheckBoxPreference pref) {
			this.pref = pref;
		}
		public CheckBoxPreference getPref() {
			return pref;
		}
	}
	
	private EditTextPreference userPassword;
	private EditTextPreference userName;
	private EditTextPreference applicationDir;
	
	private Preference saveCurrentTrack;
	private Preference reloadIndexes;
	private Preference downloadIndexes;

	private ListPreference applicationMode;
	private ListPreference daynightMode;
	private ListPreference saveTrackInterval;
	private ListPreference rotateMap;
	private ListPreference tileSourcePreference;
	private ListPreference positionOnMap;
	private ListPreference routerPreference;
	private ListPreference maxLevelToDownload;
	private ListPreference mapScreenOrientation;
	private ListPreference voicePreference;
	private ListPreference metricPreference;
	private ListPreference rendererPreference;
	private ListPreference routeServiceInterval;
	private ListPreference routeServiceWaitInterval;
	private ListPreference routeServiceProvider;
	
	private CheckBoxPreference routeServiceEnabled;
	private CheckBoxPreference useInternetToDownload;
	
	private ProgressDialog progressDlg;
	
	private BooleanPreference[] booleanPreferences = new BooleanPreference[]{
			new BooleanPreference(OsmandSettings.SHOW_VIEW_ANGLE, OsmandSettings.SHOW_VIEW_ANGLE_DEF),
			new BooleanPreference(OsmandSettings.USE_TRACKBALL_FOR_MOVEMENTS, OsmandSettings.USE_TRACKBALL_FOR_MOVEMENTS_DEF),
			new BooleanPreference(OsmandSettings.USE_HIGH_RES_MAPS, OsmandSettings.USE_HIGH_RES_MAPS_DEF),
			new BooleanPreference(OsmandSettings.USE_ENGLISH_NAMES, OsmandSettings.USE_ENGLISH_NAMES_DEF),
			new BooleanPreference(OsmandSettings.AUTO_ZOOM_MAP, OsmandSettings.AUTO_ZOOM_MAP_DEF),
			new BooleanPreference(OsmandSettings.SAVE_TRACK_TO_GPX, OsmandSettings.SAVE_TRACK_TO_GPX_DEF),
			new BooleanPreference(OsmandSettings.DEBUG_RENDERING_INFO, OsmandSettings.DEBUG_RENDERING_INFO_DEF),
			new BooleanPreference(OsmandSettings.USE_STEP_BY_STEP_RENDERING, OsmandSettings.USE_STEP_BY_STEP_RENDERING_DEF),
			new BooleanPreference(OsmandSettings.FAST_ROUTE_MODE, OsmandSettings.FAST_ROUTE_MODE_DEF),
			new BooleanPreference(OsmandSettings.USE_OSMAND_ROUTING_SERVICE_ALWAYS, OsmandSettings.USE_OSMAND_ROUTING_SERVICE_ALWAYS_DEF),
	};
	private BroadcastReceiver broadcastReceiver;
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_pref);
		PreferenceScreen screen = getPreferenceScreen();
		
		for(BooleanPreference b : booleanPreferences){
			CheckBoxPreference p = (CheckBoxPreference) screen.findPreference(b.getId());
			p.setOnPreferenceChangeListener(this);
			b.setPref(p);
		}
		
		useInternetToDownload =(CheckBoxPreference) screen.findPreference(OsmandSettings.USE_INTERNET_TO_DOWNLOAD_TILES);
		useInternetToDownload.setOnPreferenceChangeListener(this);
		
		reloadIndexes =(Preference) screen.findPreference(OsmandSettings.RELOAD_INDEXES);
		reloadIndexes.setOnPreferenceClickListener(this);
		downloadIndexes =(Preference) screen.findPreference(OsmandSettings.DOWNLOAD_INDEXES);
		downloadIndexes.setOnPreferenceClickListener(this);
		saveCurrentTrack =(Preference) screen.findPreference(OsmandSettings.SAVE_CURRENT_TRACK);
		saveCurrentTrack.setOnPreferenceClickListener(this);
		
		userName = (EditTextPreference) screen.findPreference(OsmandSettings.USER_NAME);
		userName.setOnPreferenceChangeListener(this);
		userPassword = (EditTextPreference) screen.findPreference(OsmandSettings.USER_PASSWORD);
		userPassword.setOnPreferenceChangeListener(this);
		applicationDir = (EditTextPreference) screen.findPreference(OsmandSettings.EXTERNAL_STORAGE_DIR);
		applicationDir.setOnPreferenceChangeListener(this);
		
		applicationMode =(ListPreference) screen.findPreference(OsmandSettings.APPLICATION_MODE);
		applicationMode.setOnPreferenceChangeListener(this);
		daynightMode =(ListPreference) screen.findPreference(OsmandSettings.DAYNIGHT_MODE);
		daynightMode.setOnPreferenceChangeListener(this);
		rotateMap =(ListPreference) screen.findPreference(OsmandSettings.ROTATE_MAP);
		rotateMap.setOnPreferenceChangeListener(this);
		saveTrackInterval =(ListPreference) screen.findPreference(OsmandSettings.SAVE_TRACK_INTERVAL);
		saveTrackInterval.setOnPreferenceChangeListener(this);
		positionOnMap =(ListPreference) screen.findPreference(OsmandSettings.POSITION_ON_MAP);
		positionOnMap.setOnPreferenceChangeListener(this);
		mapScreenOrientation = (ListPreference) screen.findPreference(OsmandSettings.MAP_SCREEN_ORIENTATION);
		mapScreenOrientation.setOnPreferenceChangeListener(this);
		maxLevelToDownload = (ListPreference) screen.findPreference(OsmandSettings.MAX_LEVEL_TO_DOWNLOAD_TILE);
		maxLevelToDownload.setOnPreferenceChangeListener(this);
		tileSourcePreference = (ListPreference) screen.findPreference(OsmandSettings.MAP_TILE_SOURCES);
		tileSourcePreference.setOnPreferenceChangeListener(this);
		routerPreference = (ListPreference) screen.findPreference(OsmandSettings.ROUTER_SERVICE);
		routerPreference.setOnPreferenceChangeListener(this);
		voicePreference = (ListPreference) screen.findPreference(OsmandSettings.VOICE_PROVIDER);
		voicePreference.setOnPreferenceChangeListener(this);
		metricPreference = (ListPreference) screen.findPreference(OsmandSettings.DEFAULT_METRIC_SYSTEM);
		metricPreference.setOnPreferenceChangeListener(this);
		rendererPreference =(ListPreference) screen.findPreference(OsmandSettings.RENDERER);
		rendererPreference.setOnPreferenceChangeListener(this);
		routeServiceInterval =(ListPreference) screen.findPreference(OsmandSettings.SERVICE_OFF_INTERVAL);
		routeServiceInterval.setOnPreferenceChangeListener(this);
		routeServiceWaitInterval =(ListPreference) screen.findPreference(OsmandSettings.SERVICE_OFF_WAIT_INTERVAL);
		routeServiceWaitInterval.setOnPreferenceChangeListener(this);
		routeServiceProvider =(ListPreference) screen.findPreference(OsmandSettings.SERVICE_OFF_PROVIDER);
		routeServiceProvider.setOnPreferenceChangeListener(this);
		
		routeServiceEnabled =(CheckBoxPreference) screen.findPreference(OsmandSettings.SERVICE_OFF_ENABLED);
		routeServiceEnabled.setOnPreferenceChangeListener(this);
		
		
		broadcastReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				routeServiceEnabled.setChecked(false);
			}
			
		};
		registerReceiver(broadcastReceiver, new IntentFilter(NavigationService.OSMAND_STOP_SERVICE_ACTION));
    }

	private void updateApplicationDirSummary() {
		String storageDir = OsmandSettings.getExternalStorageDirectory(getApplicationContext()).getAbsolutePath();
		applicationDir.setText(storageDir);
		applicationDir.setSummary(storageDir);
	}
    
    @Override
    protected void onResume() {
		super.onResume();
		updateAllSettings();
	}
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(broadcastReceiver);
    }
    
    
    private void fillTime(ListPreference component, int[] seconds, int[] minutes, int currentSeconds){
    	int minutesLength = minutes == null? 0 : minutes.length;
    	int secondsLength = seconds == null? 0 : seconds.length;
    	
    	
    	String[] ints = new String[secondsLength + minutesLength];
		String[] intDescriptions = new String[ints.length];
		for (int i = 0; i < secondsLength; i++) {
			ints[i] = seconds[i] + "";
			intDescriptions[i] = ints[i] + " " + getString(R.string.int_seconds); //$NON-NLS-1$
		}
		for (int i = 0; i < minutesLength; i++) {
			ints[secondsLength + i] = (minutes[i] * 60) + "";
			intDescriptions[secondsLength + i] = minutes[i] + " " + getString(R.string.int_min); //$NON-NLS-1$
		}
		fill(component, intDescriptions, ints, currentSeconds+"");
    }
    
    public void updateAllSettings(){
    	SharedPreferences prefs = getSharedPreferences(OsmandSettings.SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
    	for(BooleanPreference b : booleanPreferences){
    		b.getPref().setChecked(prefs.getBoolean(b.getId(), b.getDefValue()));
    	}
		userName.setText(OsmandSettings.getUserName(prefs));
		userPassword.setText(OsmandSettings.getUserPassword(prefs));
		applicationDir.setText(OsmandSettings.getExternalStorageDirectory(prefs).getAbsolutePath());
		useInternetToDownload.setChecked(OsmandSettings.isUsingInternetToDownloadTiles(prefs));
		
		Resources resources = this.getResources();
		String[] e = new String[] {resources.getString(R.string.position_on_map_center), 
				resources.getString(R.string.position_on_map_bottom)};
		positionOnMap.setEntryValues(e);
		positionOnMap.setEntries(e);
		positionOnMap.setValueIndex(OsmandSettings.getPositionOnMap(prefs));
		
		fillTime(saveTrackInterval, new int[]{1, 2, 3, 5, 15, 20, 30}, new int[]{1, 2, 3, 5}, OsmandSettings.getSavingTrackInterval(prefs)); //$NON-NLS-1$
		
		fillTime(routeServiceInterval, new int[]{0, 30, 45, 60}, new int[]{2, 3, 5, 8, 10, 15, 20, 30, 40, 50, 70, 90}, OsmandSettings.getServiceOffInterval(prefs)/1000); //$NON-NLS-1$
		
		fillTime(routeServiceWaitInterval, new int[]{15, 30, 45, 60, 90}, new int[]{2, 3, 5, 10}, OsmandSettings.getServiceOffWaitInterval(prefs)/1000);
		
		fill(rotateMap, //
				new String[]{getString(R.string.rotate_map_none_opt), getString(R.string.rotate_map_bearing_opt), getString(R.string.rotate_map_compass_opt)}, //
				new String[]{OsmandSettings.ROTATE_MAP_NONE+"", OsmandSettings.ROTATE_MAP_BEARING+"", OsmandSettings.ROTATE_MAP_COMPASS+""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				OsmandSettings.getRotateMap(prefs)+""); //$NON-NLS-1$
		
		fill(routeServiceProvider,//
				new String[]{getString(R.string.gps_provider), getString(R.string.network_provider)}, //
				new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}, //
				OsmandSettings.getServiceOffProvider(prefs));
		
		routeServiceEnabled.setChecked(getMyApplication().getNavigationService() != null);

		fill(mapScreenOrientation, //
				new String[] {
						resources.getString(R.string.map_orientation_portrait),
						resources.getString(R.string.map_orientation_landscape),
						resources.getString(R.string.map_orientation_default), }, //
				new String[] {
						ActivityInfo.SCREEN_ORIENTATION_PORTRAIT + "", //$NON-NLS-1$
						ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE + "", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED + "" }, //$NON-NLS-1$ //$NON-NLS-2$
				OsmandSettings.getMapOrientation(prefs) + ""); //$NON-NLS-1$
		
		ApplicationMode[] presets = ApplicationMode.values(); 
		String[] names = new String[presets.length];
		String[] values = new String[presets.length];
		for(int i=0; i<presets.length; i++){
			names[i] = ApplicationMode.toHumanString(presets[i], this);
			values[i] = presets[i].name();
		}
		fill(applicationMode, names, values, OsmandSettings.getApplicationMode(prefs).name());

		DayNightMode[] dnpresets = DayNightMode.possibleValues(this); 
		names = new String[dnpresets.length];
		values = new String[dnpresets.length];
		for(int i=0; i< dnpresets.length; i++){
			names[i] = dnpresets[i].toHumanString(this);
			values[i] = dnpresets[i].name();
		}
		fill(daynightMode, names, values, OsmandSettings.getDayNightMode(prefs).name());
		
		String[] entries = new String[RouteService.values().length];
		String entry = OsmandSettings.getRouterService(prefs).getName();
		for (int i = 0; i < RouteService.values().length; i++) {
			entries[i] = RouteService.values()[i].getName();
		}
		fill(routerPreference, entries, entries, entry);
		
		names = new String[MetricsConstants.values().length];
		values = new String[MetricsConstants.values().length];
		entry = OsmandSettings.getDefaultMetricConstants(this).name();
		for (int i = 0; i < MetricsConstants.values().length; i++) {
			values[i] = MetricsConstants.values()[i].name();
			names[i] = MetricsConstants.values()[i].toHumanString(this);
		}
		fill(metricPreference, names, values, entry);

		// read available voice data
		File extStorage = OsmandSettings.extendOsmandPath(getApplicationContext(), ResourceManager.VOICE_PATH);
		Set<String> setFiles = new LinkedHashSet<String>();
		if (extStorage.exists()) {
			for (File f : extStorage.listFiles()) {
				if (f.isDirectory()) {
					setFiles.add(f.getName());
				}
			}
		}
		String provider = OsmandSettings.getVoiceProvider(prefs);
		entries = new String[setFiles.size() + 1];
		int k = 0; 
		entries[k++] = getString(R.string.voice_not_use);
		for(String s : setFiles){
			entries[k++] = s;
		}
		voicePreference.setEntries(entries);
		voicePreference.setEntryValues(entries);
		if(setFiles.contains(provider)){
			voicePreference.setValue(provider);
		} else {
			voicePreference.setValueIndex(0);
		}
		
		String vectorRenderer = OsmandSettings.getVectorRenderer(prefs);
		Collection<String> rendererNames = RendererRegistry.getRegistry().getRendererNames();
		entries = (String[]) rendererNames.toArray(new String[rendererNames.size()]);
		rendererPreference.setEntries(entries);
		rendererPreference.setEntryValues(entries);
		if(rendererNames.contains(vectorRenderer)){
			rendererPreference.setValue(vectorRenderer);
		} else {
			rendererPreference.setValueIndex(0);
		}
		
		int startZoom = 12;
		int endZoom = 19;
		entries = new String[endZoom - startZoom + 1];
		for (int i = startZoom; i <= endZoom; i++) {
			entries[i - startZoom] = i + ""; //$NON-NLS-1$
		}
		fill(maxLevelToDownload, entries, entries, OsmandSettings.getMaximumLevelToDownloadTile(prefs)+""); //$NON-NLS-1$

		Map<String, String> entriesMap = getTileSourceEntries(this);
		entries = new String[entriesMap.size() + 1];
		values = new String[entriesMap.size() + 1];
		values[0] = VECTOR_MAP;
		entries[0] = getString(R.string.vector_data);
		int ki = 1;
		for(Map.Entry<String, String> es : entriesMap.entrySet()){
			entries[ki] = es.getValue();
			values[ki] = es.getKey();
			ki++;
		}
		String value = OsmandSettings.isUsingMapVectorData(prefs)? VECTOR_MAP : OsmandSettings.getMapTileSourceName(prefs);
		fill(tileSourcePreference, entries, values, value);

		String mapName = " " + (OsmandSettings.isUsingMapVectorData(prefs) ? getString(R.string.vector_data) : //$NON-NLS-1$
				OsmandSettings.getMapTileSourceName(prefs));
		String summary = tileSourcePreference.getSummary().toString();
		if (summary.lastIndexOf(':') != -1) {
			summary = summary.substring(0, summary.lastIndexOf(':') + 1);
		}
		tileSourcePreference.setSummary(summary + mapName);
		
		updateApplicationDirSummary();
    }
    
	private void fill(ListPreference component, String[] list, String[] values, String selected) {
		component.setEntries(list);
		component.setEntryValues(values);
		component.setValue(selected);
	}
    
    public static Map<String, String> getTileSourceEntries(Context ctx){
		Map<String, String> map = new LinkedHashMap<String, String>();
		File dir = OsmandSettings.extendOsmandPath(ctx, ResourceManager.TILES_PATH);
		if (dir != null && dir.canRead()) {
			File[] files = dir.listFiles();
			Arrays.sort(files, new Comparator<File>(){
				@Override
				public int compare(File object1, File object2) {
					if(object1.lastModified() > object2.lastModified()){
						return -1;
					} else if(object1.lastModified() == object2.lastModified()){
						return 0;
					}
					return 1;
				}
				
			});
			if (files != null) {
				for (File f : files) {
					if (f.getName().endsWith(SQLiteTileSource.EXT)) {
						String n = f.getName();
						map.put(f.getName(), n.substring(0, n.lastIndexOf('.')));
					} else if (f.isDirectory() && !f.getName().equals(ResourceManager.TEMP_SOURCE_TO_LOAD)) {
						map.put(f.getName(), f.getName());
					}
				}
			}
		}
		for(TileSourceTemplate l : TileSourceManager.getKnownSourceTemplates()){
			map.put(l.getName(), l.getName());
		}
		return map;
		
    }
    

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		SharedPreferences prefs = getSharedPreferences(OsmandSettings.SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		Editor edit = prefs.edit();
		// handle boolean prefences
		BooleanPreference p = null;
		for(BooleanPreference b : booleanPreferences){
			if(b.getPref() == preference){
				p = b;
				break;
			}
		}
		if(p != null){
			edit.putBoolean(p.getId(), (Boolean)newValue);
			if(p.getId() == OsmandSettings.SHOW_POI_OVER_MAP && ((Boolean) newValue)){
				edit.putString(OsmandSettings.SELECTED_POI_FILTER_FOR_MAP, PoiFiltersHelper.getOsmDefinedFilterId(null));
			}
			
			edit.commit();
			
		} else if(preference == applicationMode){
			ApplicationMode old = OsmandSettings.getApplicationMode(prefs);
			edit.putString(OsmandSettings.APPLICATION_MODE, (String) newValue);
			setAppMode(ApplicationMode.valueOf(newValue.toString()), edit, getMyApplication(), old);
			edit.commit();
			updateAllSettings();
		} else if(preference == daynightMode){
			edit.putString(OsmandSettings.DAYNIGHT_MODE, (String) newValue);
			getMyApplication().getDaynightHelper().setDayNightMode(DayNightMode.valueOf(newValue.toString()));
			edit.commit();
		} else if(preference == mapScreenOrientation){
			edit.putInt(OsmandSettings.MAP_SCREEN_ORIENTATION, Integer.parseInt(newValue.toString()));
			edit.commit();
		} else if(preference == saveTrackInterval){
			edit.putInt(OsmandSettings.SAVE_TRACK_INTERVAL, Integer.parseInt(newValue.toString()));
			edit.commit();
		} else if(preference == userPassword){
			edit.putString(OsmandSettings.USER_PASSWORD, (String) newValue);
			edit.commit();
		} else if(preference == useInternetToDownload){
			OsmandSettings.setUseInternetToDownloadTiles((Boolean) newValue, edit);
			edit.commit();
		} else if(preference == userName){
			edit.putString(OsmandSettings.USER_NAME, (String) newValue);
			edit.commit();
		} else if(preference == applicationDir){
			warnAboutChangingStorage(edit, (String) newValue);
			return false;
		} else if(preference == positionOnMap){
			edit.putInt(OsmandSettings.POSITION_ON_MAP, positionOnMap.findIndexOfValue((String) newValue));
			edit.commit();
		} else if (preference == maxLevelToDownload) {
			edit.putInt(OsmandSettings.MAX_LEVEL_TO_DOWNLOAD_TILE, Integer.parseInt((String) newValue));
			edit.commit();
		} else if (preference == routeServiceInterval) {
			edit.putInt(OsmandSettings.SERVICE_OFF_INTERVAL, Integer.parseInt((String) newValue) * 1000);
			edit.commit();
		} else if (preference == routeServiceWaitInterval) {
			edit.putInt(OsmandSettings.SERVICE_OFF_WAIT_INTERVAL, Integer.parseInt((String) newValue) * 1000);
			edit.commit();
		} else if (preference == rotateMap) {
			edit.putInt(OsmandSettings.ROTATE_MAP, Integer.parseInt((String) newValue));
			edit.commit();
		} else if (preference == routeServiceProvider) {
			edit.putString(OsmandSettings.SERVICE_OFF_PROVIDER, (String) newValue);
			edit.commit();
		} else if (preference == routeServiceEnabled) {
			Intent serviceIntent = new Intent(this, NavigationService.class);
			if ((Boolean) newValue) {
				ComponentName name = startService(serviceIntent);
				if (name == null) {
					routeServiceEnabled.setChecked(getMyApplication().getNavigationService() != null);
				}
			} else {
				if(!stopService(serviceIntent)){
					routeServiceEnabled.setChecked(getMyApplication().getNavigationService() != null);
				}
			}
		} else if (preference == routerPreference) {
			RouteService s = null;
			for(RouteService r : RouteService.values()){
				if(r.getName().equals(newValue)){
					s = r;
					break;
				}
			}
			if(s != null){
				edit.putInt(OsmandSettings.ROUTER_SERVICE, s.ordinal());
			}
			edit.commit();
		} else if (preference == rendererPreference) {
			BaseOsmandRender loaded = RendererRegistry.getRegistry().getRenderer((String) newValue);
			if(loaded == null){
				Toast.makeText(this, R.string.renderer_load_exception, Toast.LENGTH_SHORT).show();
			} else {
				RendererRegistry.getRegistry().setCurrentSelectedRender(loaded);
				edit.putString(OsmandSettings.RENDERER, (String) newValue);
				Toast.makeText(this, R.string.renderer_load_sucess, Toast.LENGTH_SHORT).show();
				getMyApplication().getResourceManager().getRenderer().clearCache();
			}
			edit.commit();
		} else if (preference == voicePreference) {
			int i = voicePreference.findIndexOfValue((String) newValue);
			if (i == 0) {
				edit.putString(OsmandSettings.VOICE_PROVIDER, null);
			} else {
				edit.putString(OsmandSettings.VOICE_PROVIDER, (String) newValue);
			}
			edit.commit();
			getMyApplication().initCommandPlayer();
		} else if (preference == metricPreference) {
			MetricsConstants mc = MetricsConstants.valueOf((String) newValue);
			OsmandSettings.setDefaultMetricConstants(edit, mc);
		} else if (preference == tileSourcePreference) {
			if(VECTOR_MAP.equals((String) newValue)){
				edit.putBoolean(OsmandSettings.MAP_VECTOR_DATA, true);
			} else {
				edit.putString(OsmandSettings.MAP_TILE_SOURCES, (String) newValue);
				edit.putBoolean(OsmandSettings.MAP_VECTOR_DATA, false);
			}
			edit.commit();
			String summary = tileSourcePreference.getSummary().toString();
			if (summary.lastIndexOf(':') != -1) {
				summary = summary.substring(0, summary.lastIndexOf(':') + 1);
			}
			summary += " " + (OsmandSettings.isUsingMapVectorData(prefs) ? getString(R.string.vector_data) : //$NON-NLS-1$
				OsmandSettings.getMapTileSourceName(prefs));
			tileSourcePreference.setSummary(summary);
			
		}
		return true;
	}

	private void warnAboutChangingStorage(final Editor edit, final String newValue) {
		final String newDir = newValue != null ? newValue.trim(): newValue;
		File path = new File(newDir);
		path.mkdirs();
		if(!path.canRead() || !path.exists()){
			Toast.makeText(this, R.string.specified_dir_doesnt_exist, Toast.LENGTH_LONG).show()	;
			return;
		}
		
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.application_dir_change_warning));
		builder.setPositiveButton(R.string.default_buttons_yes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//edit the preference
				edit.putString(OsmandSettings.EXTERNAL_STORAGE_DIR, newDir);
				edit.commit();
				getMyApplication().getResourceManager().resetStoreDirectory();
				reloadIndexes();
				updateApplicationDirSummary();
			}
		});
		builder.setNegativeButton(R.string.default_buttons_cancel, null);
		builder.show();
	}

	public void reloadIndexes(){
		progressDlg = ProgressDialog.show(this, getString(R.string.loading_data), getString(R.string.reading_indexes), true);
		final ProgressDialogImplementation impl = new ProgressDialogImplementation(progressDlg);
		impl.setRunnable("Initializing app", new Runnable(){ //$NON-NLS-1$
			@Override
			public void run() {
				try {
					showWarnings(getMyApplication().getResourceManager().reloadIndexes(impl));
				} finally {
					if(progressDlg !=null){
						progressDlg.dismiss();
						progressDlg = null;
					}
				}
			}
		});
		impl.run();
	}
	
	private OsmandApplication getMyApplication() {
		return (OsmandApplication)getApplication();
	}
	
	@Override
	protected void onStop() {
		if(progressDlg !=null){
			progressDlg.dismiss();
			progressDlg = null;
		}
		super.onStop();
	}
	protected void showWarnings(List<String> warnings) {
		if (!warnings.isEmpty()) {
			final StringBuilder b = new StringBuilder();
			boolean f = true;
			for (String w : warnings) {
				if(f){
					f = false;
				} else {
					b.append('\n');
				}
				b.append(w);
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(SettingsActivity.this, b.toString(), Toast.LENGTH_LONG).show();

				}
			});
		}
	}
		
	public static void setAppMode(ApplicationMode preset, Editor edit, OsmandApplication application, ApplicationMode old){
		if (preset == ApplicationMode.CAR) {
			OsmandSettings.setUseInternetToDownloadTiles(true, edit);
			// edit.putBoolean(OsmandSettings.SHOW_POI_OVER_MAP, _);
			edit.putBoolean(OsmandSettings.SHOW_TRANSPORT_OVER_MAP, false);
			edit.putInt(OsmandSettings.ROTATE_MAP, OsmandSettings.ROTATE_MAP_BEARING);
			edit.putBoolean(OsmandSettings.SHOW_VIEW_ANGLE, false);
			edit.putBoolean(OsmandSettings.AUTO_ZOOM_MAP, true);
			edit.putBoolean(OsmandSettings.SHOW_OSM_BUGS, false);
			edit.putBoolean(OsmandSettings.USE_STEP_BY_STEP_RENDERING, true);
			// edit.putBoolean(OsmandSettings.USE_ENGLISH_NAMES, _);
			edit.putBoolean(OsmandSettings.SAVE_TRACK_TO_GPX, true);
			edit.putInt(OsmandSettings.SAVE_TRACK_INTERVAL, 5);
			edit.putInt(OsmandSettings.POSITION_ON_MAP, OsmandSettings.BOTTOM_CONSTANT);
			// edit.putString(OsmandSettings.MAP_TILE_SOURCES, _);

		} else if (preset == ApplicationMode.BICYCLE) {
			// edit.putBoolean(OsmandSettings.USE_INTERNET_TO_DOWNLOAD_TILES, _);
			// edit.putBoolean(OsmandSettings.USE_INTERNET_TO_CALCULATE_ROUTE, _);
			// edit.putBoolean(OsmandSettings.SHOW_POI_OVER_MAP, true);
			edit.putInt(OsmandSettings.ROTATE_MAP, OsmandSettings.ROTATE_MAP_BEARING);
			edit.putBoolean(OsmandSettings.SHOW_VIEW_ANGLE, true);
			edit.putBoolean(OsmandSettings.AUTO_ZOOM_MAP, false);
			// edit.putBoolean(OsmandSettings.SHOW_OSM_BUGS, _);
			// edit.putBoolean(OsmandSettings.USE_ENGLISH_NAMES, _);
			edit.putBoolean(OsmandSettings.SAVE_TRACK_TO_GPX, true);
			edit.putInt(OsmandSettings.SAVE_TRACK_INTERVAL, 30);
			edit.putInt(OsmandSettings.POSITION_ON_MAP, OsmandSettings.BOTTOM_CONSTANT);
			// edit.putString(OsmandSettings.MAP_TILE_SOURCES, _);

		} else if (preset == ApplicationMode.PEDESTRIAN) {
			// edit.putBoolean(OsmandSettings.USE_INTERNET_TO_DOWNLOAD_TILES, _);
			// edit.putBoolean(OsmandSettings.SHOW_POI_OVER_MAP, true);
			edit.putInt(OsmandSettings.ROTATE_MAP, OsmandSettings.ROTATE_MAP_COMPASS);
			edit.putBoolean(OsmandSettings.SHOW_VIEW_ANGLE, true);
			edit.putBoolean(OsmandSettings.AUTO_ZOOM_MAP, false);
			edit.putBoolean(OsmandSettings.USE_STEP_BY_STEP_RENDERING, false);
			// if(useInternetToDownloadTiles.isChecked()){
			// edit.putBoolean(OsmandSettings.SHOW_OSM_BUGS, true);
			// }
			// edit.putBoolean(OsmandSettings.USE_ENGLISH_NAMES, _);
			edit.putBoolean(OsmandSettings.SAVE_TRACK_TO_GPX, false);
			// edit.putInt(OsmandSettings.SAVE_TRACK_INTERVAL, _);
			edit.putInt(OsmandSettings.POSITION_ON_MAP, OsmandSettings.CENTER_CONSTANT);
			// edit.putString(OsmandSettings.MAP_TILE_SOURCES, _);

		} else if (preset == ApplicationMode.DEFAULT) {
			// edit.putBoolean(OsmandSettings.USE_INTERNET_TO_DOWNLOAD_TILES, _);
			// edit.putBoolean(OsmandSettings.SHOW_POI_OVER_MAP, true);
			edit.putInt(OsmandSettings.ROTATE_MAP, OsmandSettings.ROTATE_MAP_NONE);
			edit.putBoolean(OsmandSettings.SHOW_VIEW_ANGLE, false);
			edit.putBoolean(OsmandSettings.AUTO_ZOOM_MAP, false);
			edit.putBoolean(OsmandSettings.USE_STEP_BY_STEP_RENDERING, true);
			// edit.putBoolean(OsmandSettings.SHOW_OSM_BUGS, _);
			// edit.putBoolean(OsmandSettings.USE_ENGLISH_NAMES, _);
			edit.putBoolean(OsmandSettings.SAVE_TRACK_TO_GPX, false);
			// edit.putInt(OsmandSettings.SAVE_TRACK_INTERVAL, _);
			edit.putInt(OsmandSettings.POSITION_ON_MAP, OsmandSettings.CENTER_CONSTANT);
			// edit.putString(OsmandSettings.MAP_TILE_SOURCES, _);

		}

		BaseOsmandRender current = RendererRegistry.getRegistry().getCurrentSelectedRenderer();
		BaseOsmandRender defaultRender = RendererRegistry.getRegistry().defaultRender();
		BaseOsmandRender newRenderer;
		if (preset == ApplicationMode.CAR) {
			newRenderer = RendererRegistry.getRegistry().carRender();
		} else if (preset == ApplicationMode.BICYCLE) {
			newRenderer = RendererRegistry.getRegistry().bicycleRender();
		} else if (preset == ApplicationMode.PEDESTRIAN) {
			newRenderer = RendererRegistry.getRegistry().pedestrianRender();
		} else {
			newRenderer = defaultRender;
		}
		if (newRenderer != current) {
			RendererRegistry.getRegistry().setCurrentSelectedRender(newRenderer);
			application.getResourceManager().getRenderer().clearCache();
		}
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == applicationDir) {
			return true;
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference == downloadIndexes){
			startActivity(new Intent(this, DownloadIndexActivity.class));
			return true;
		} else if(preference == reloadIndexes){
			reloadIndexes();
			return true;
		} else if(preference == saveCurrentTrack){
			SavingTrackHelper helper = new SavingTrackHelper(this);
			if (helper.hasDataToSave()) {
				progressDlg = ProgressDialog.show(this, getString(R.string.saving_gpx_tracks), getString(R.string.saving_gpx_tracks), true);
				final ProgressDialogImplementation impl = new ProgressDialogImplementation(progressDlg);
				impl.setRunnable("SavingGPX", new Runnable() { //$NON-NLS-1$
					@Override
					public void run() {
							try {
								SavingTrackHelper helper = new SavingTrackHelper(SettingsActivity.this);
								helper.saveDataToGpx();
								helper.close();
							} finally {
								if (progressDlg != null) {
									progressDlg.dismiss();
									progressDlg = null;
								}
							}
						}
					});
				impl.run();
			} else {
				helper.close();
			}
			return true;
		}
		return false;
	}
}
