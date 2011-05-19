package pl.edu.agh.assembler;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.model.LocationInfo;

public class LocationInfoJSONAssembler extends AbstractJSONAssembler<LocationInfo> {

	private static final String DIRECTION_PARAM = "dir";
	private static final String ACCURACY_PARAM = "acc";
	private static final String SPEED_PARAM = "spd";
	private static final String TIME_PARAM = "time";

	private SimpleLocationInfoJSONAssembler simpleAssembler;

	public LocationInfoJSONAssembler(SimpleLocationInfoJSONAssembler simpleAssembler) {
		this.simpleAssembler = simpleAssembler;
	}

	@Override
	public JSONObject serialize(LocationInfo info) throws JSONException {
		JSONObject serializedInfo = simpleAssembler.serialize(info);

		serializedInfo.put(TIME_PARAM, info.getTime());
		serializedInfo.put(SPEED_PARAM, info.getSpeed());
		serializedInfo.put(ACCURACY_PARAM, info.getAccuracy());
		serializedInfo.put(DIRECTION_PARAM, info.getDirection());

		return serializedInfo;
	}

	@Override
	public LocationInfo deserialize(JSONObject serializedInfo) throws JSONException {
		LocationInfo info = new LocationInfo();

		simpleAssembler.deserialize(serializedInfo, info);

		info.setTime(serializedInfo.getLong(TIME_PARAM));
		info.setSpeed(serializedInfo.getDouble(SPEED_PARAM));
		info.setAccuracy(serializedInfo.getDouble(ACCURACY_PARAM));
		info.setDirection(serializedInfo.getDouble(DIRECTION_PARAM));

		return info;
	}

}
