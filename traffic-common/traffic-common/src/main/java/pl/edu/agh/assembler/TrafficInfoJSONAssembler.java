package pl.edu.agh.assembler;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.model.TrafficInfo;

public class TrafficInfoJSONAssembler extends AbstractJSONAssembler<TrafficInfo> {

	private static final String REVERSE_WAY_SPEED_PARAM = "reverse";
	private static final String DIRECT_WAY_SPEED_PARAM = "direct";
	private static final String WAY_PARAM = "way";
	private SimpleLocationInfoJSONAssembler simpleLocationInfoJSONAssembler;

	public TrafficInfoJSONAssembler(SimpleLocationInfoJSONAssembler simpleLocationInfoJSONAssembler) {
		this.simpleLocationInfoJSONAssembler = simpleLocationInfoJSONAssembler;
	}

	@Override
	public JSONObject serialize(TrafficInfo info) throws JSONException {
		JSONObject serializedInfo = new JSONObject();

		serializedInfo.put(WAY_PARAM, simpleLocationInfoJSONAssembler.serialize(info.getWay()));
		serializedInfo.put(DIRECT_WAY_SPEED_PARAM, info.getDirectWaySpeed());
		serializedInfo.put(REVERSE_WAY_SPEED_PARAM, info.getReverseWaySpeed());

		return serializedInfo;
	}

	@Override
	public TrafficInfo deserialize(JSONObject serializedInfo) throws JSONException {
		List<SimpleLocationInfo> way = simpleLocationInfoJSONAssembler.deserialize(serializedInfo
				.getJSONArray(WAY_PARAM));
		Double directWaySpeed = serializedInfo.getDouble(DIRECT_WAY_SPEED_PARAM);
		Double reverseWaySpeed = serializedInfo.has(REVERSE_WAY_SPEED_PARAM) ? serializedInfo
				.getDouble(REVERSE_WAY_SPEED_PARAM) : null;

		return new TrafficInfo(way, directWaySpeed, reverseWaySpeed);
	}

}
