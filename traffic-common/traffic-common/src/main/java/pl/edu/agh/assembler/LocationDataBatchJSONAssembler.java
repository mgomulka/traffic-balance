package pl.edu.agh.assembler;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.model.LocationDataBatch;
import pl.edu.agh.model.LocationInfo;

public class LocationDataBatchJSONAssembler {

	private static final String DIRECTION_PARAM = "dir";
	private static final String ACCURACY_PARAM = "acc";
	private static final String SPEED_PARAM = "spd";
	private static final String TIME_PARAM = "time";
	private static final String LATITUDE_PARAM = "lat";
	private static final String LONGITUTUDE_PARAM = "lon";
	private static final String INFOS_PARAM = "infos";

	public static JSONObject serialize(LocationDataBatch locationDataBatch) throws JSONException {
		JSONObject jsonBatch = new JSONObject();
		jsonBatch.put(INFOS_PARAM, serialize(locationDataBatch.getLocationInfos()));

		return jsonBatch;
	}

	private static JSONArray serialize(List<LocationInfo> locationInfos) throws JSONException {
		JSONArray jsonInfos = new JSONArray();
		for (LocationInfo info : locationInfos) {
			jsonInfos.put(serialize(info));
		}

		return jsonInfos;
	}

	private static JSONObject serialize(LocationInfo info) throws JSONException {
		JSONObject jsonInfo = new JSONObject();

		jsonInfo.put(LONGITUTUDE_PARAM, info.getLongitude());
		jsonInfo.put(LATITUDE_PARAM, info.getLatitude());
		jsonInfo.put(TIME_PARAM, info.getTime());
		jsonInfo.put(SPEED_PARAM, info.getSpeed());
		jsonInfo.put(ACCURACY_PARAM, info.getAccuracy());
		jsonInfo.put(DIRECTION_PARAM, info.getDirection());

		return jsonInfo;
	}
	
	public static LocationDataBatch deserialize(JSONObject jsonBatch) throws JSONException {
		List<LocationInfo> infos = deserializeInfos(jsonBatch.getJSONArray(INFOS_PARAM));
		
		return new LocationDataBatch(infos);
	}

	private static List<LocationInfo> deserializeInfos(JSONArray jsonInfos) throws JSONException {
		List<LocationInfo> infos = new ArrayList<LocationInfo>();
		
		for (int i = 0; i < jsonInfos.length(); i++) {
			infos.add(deserializeInfo(jsonInfos.getJSONObject(i)));
		}
		
		return infos;
	}

	private static LocationInfo deserializeInfo(JSONObject jsonInfo) throws JSONException {
		LocationInfo info = new LocationInfo();
		
		info.setLongitude(jsonInfo.getDouble(LONGITUTUDE_PARAM));
		info.setLatitude(jsonInfo.getDouble(LATITUDE_PARAM));
		info.setTime(jsonInfo.getLong(TIME_PARAM));
		info.setSpeed(jsonInfo.getDouble(SPEED_PARAM));
		info.setAccuracy(jsonInfo.getDouble(ACCURACY_PARAM));
		info.setDirection(jsonInfo.getDouble(DIRECTION_PARAM));
		
		return info;
	}
}
