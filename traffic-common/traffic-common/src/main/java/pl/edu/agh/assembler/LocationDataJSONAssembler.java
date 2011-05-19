package pl.edu.agh.assembler;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.LocationInfo;

public class LocationDataJSONAssembler extends AbstractJSONAssembler<LocationData> {

	private static final String INFOS_PARAM = "infos";

	private LocationInfoJSONAssembler locationInfoAssembler;

	public LocationDataJSONAssembler(LocationInfoJSONAssembler locationInfoAssembler) {
		this.locationInfoAssembler = locationInfoAssembler;
	}

	@Override
	public JSONObject serialize(LocationData data) throws JSONException {
		JSONObject serializedData = new JSONObject();

		serializedData.put(INFOS_PARAM, locationInfoAssembler.serialize(data.getLocationInfos()));

		return serializedData;
	}

	@Override
	public LocationData deserialize(JSONObject serializedEntity) throws JSONException {
		List<LocationInfo> infos = locationInfoAssembler.deserialize(serializedEntity.getJSONArray(INFOS_PARAM));

		return new LocationData(infos);
	}
}
