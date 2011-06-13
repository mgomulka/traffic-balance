package pl.edu.agh.assembler;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.model.TrafficData;
import pl.edu.agh.model.TrafficInfo;

public class TrafficDataJSONAssembler extends AbstractJSONAssembler<TrafficData> {

	private static final String INFOS_PARAM = "infos";

	private TrafficInfoJSONAssembler trafficInfoJSONAssembler;

	public TrafficDataJSONAssembler(TrafficInfoJSONAssembler trafficInfoJSONAssembler) {
		this.trafficInfoJSONAssembler = trafficInfoJSONAssembler;
	}

	@Override
	public JSONObject serialize(TrafficData data) throws JSONException {
		JSONObject serializedData = new JSONObject();

		serializedData.put(INFOS_PARAM, trafficInfoJSONAssembler.serialize(data.getTrafficInfos()));

		return serializedData;
	}

	@Override
	public TrafficData deserialize(JSONObject serializedData) throws JSONException {
		List<TrafficInfo> infos = trafficInfoJSONAssembler.deserialize(serializedData.getJSONArray(INFOS_PARAM));

		return new TrafficData(infos);
	}
}
