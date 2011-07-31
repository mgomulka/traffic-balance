package pl.edu.agh.assembler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.model.LocationInfo;

public class LocationInfoJSONAssembler extends AbstractJSONAssembler<LocationInfo> {

	private static final String DIRECTION_PARAM = "dir";
	private static final String ACCURACY_PARAM = "acc";
	private static final String SPEED_PARAM = "spd";
	private static final String TIME_PARAM = "time";
	
	private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

	private SimpleLocationInfoJSONAssembler simpleAssembler;

	public LocationInfoJSONAssembler(SimpleLocationInfoJSONAssembler simpleAssembler) {
		this.simpleAssembler = simpleAssembler;
	}

	@Override
	public JSONObject serialize(LocationInfo info) throws JSONException {
		DateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
		JSONObject serializedInfo = simpleAssembler.serialize(info);

		serializedInfo.put(TIME_PARAM, formatter.format(info.getTime()));
		serializedInfo.put(SPEED_PARAM, info.getSpeed());
		serializedInfo.put(ACCURACY_PARAM, info.getAccuracy());
		serializedInfo.put(DIRECTION_PARAM, info.getDirection());

		return serializedInfo;
	}

	@Override
	public LocationInfo deserialize(JSONObject serializedInfo) throws JSONException {
		DateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
		LocationInfo info = new LocationInfo();

		simpleAssembler.deserialize(serializedInfo, info);

		try {
			info.setTime(formatter.parse(serializedInfo.getString(TIME_PARAM)));
		} catch (ParseException e) {
		}
		info.setSpeed(serializedInfo.getDouble(SPEED_PARAM));
		info.setAccuracy(serializedInfo.getDouble(ACCURACY_PARAM));
		info.setDirection(serializedInfo.getDouble(DIRECTION_PARAM));

		return info;
	}

}
