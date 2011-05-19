package pl.edu.agh.assembler;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.model.TrafficData;

public class TrafficDataJSONAssembler extends AbstractJSONAssembler<TrafficData> {

	@Override
	public JSONObject serialize(TrafficData data) throws JSONException {
		JSONObject serializedData = new JSONObject();

		serializedData.put("number", data.getNumber());

		return serializedData;
	}

	@Override
	public TrafficData deserialize(JSONObject serializedData) throws JSONException {
		TrafficData data = new TrafficData();

		data.setNumber(serializedData.getInt("number"));

		return data;
	}
}
