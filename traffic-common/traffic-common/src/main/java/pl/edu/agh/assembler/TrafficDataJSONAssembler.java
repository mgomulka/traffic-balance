package pl.edu.agh.assembler;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.model.TrafficData;

public class TrafficDataJSONAssembler {

	public static JSONObject serialize(TrafficData trafficData) throws JSONException {
		JSONObject jsonTrafficData = new JSONObject();

		jsonTrafficData.put("number", trafficData.getNumber());

		return jsonTrafficData;
	}

	public static TrafficData deserialize(JSONObject jsonTrafficData) throws JSONException {
		TrafficData trafficData = new TrafficData();

		trafficData.setNumber(jsonTrafficData.getInt("number"));

		return trafficData;
	}
}
