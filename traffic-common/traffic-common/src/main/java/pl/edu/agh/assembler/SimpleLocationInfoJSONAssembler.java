package pl.edu.agh.assembler;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.model.SimpleLocationInfo;

public class SimpleLocationInfoJSONAssembler extends AbstractJSONAssembler<SimpleLocationInfo> {

	private static final String LATITUDE_PARAM = "lat";
	private static final String LONGITUTUDE_PARAM = "lon";

	@Override
	public JSONObject serialize(SimpleLocationInfo info) throws JSONException {
		JSONObject serializedInfo = new JSONObject();

		serializedInfo.put(LONGITUTUDE_PARAM, info.getLongitude());
		serializedInfo.put(LATITUDE_PARAM, info.getLatitude());

		return serializedInfo;
	}

	@Override
	public SimpleLocationInfo deserialize(JSONObject serializedInfo) throws JSONException {
		SimpleLocationInfo info = new SimpleLocationInfo();

		deserialize(serializedInfo, info);

		return info;
	}
	
	void deserialize(JSONObject serializedInfo, SimpleLocationInfo info) throws JSONException {
		info.setLongitude(serializedInfo.getDouble(LONGITUTUDE_PARAM));
		info.setLatitude(serializedInfo.getDouble(LATITUDE_PARAM));
	}

}
