package pl.edu.agh.assembler;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.model.RoutingResult;
import pl.edu.agh.model.SimpleLocationInfo;

public class RoutingResultJSONAssembler extends AbstractJSONAssembler<RoutingResult> {

	private static final String LOCATIONS_PARAM = "locs";

	private SimpleLocationInfoJSONAssembler locationAssembler;

	public RoutingResultJSONAssembler(SimpleLocationInfoJSONAssembler locationAssembler) {
		this.locationAssembler = locationAssembler;
	}

	@Override
	public JSONObject serialize(RoutingResult routingResult) throws JSONException {
		JSONObject serializedRoutingResult = new JSONObject();

		serializedRoutingResult.put(LOCATIONS_PARAM, locationAssembler.serialize(routingResult.getLocations()));

		return serializedRoutingResult;
	}

	@Override
	public RoutingResult deserialize(JSONObject serializedEntity) throws JSONException {
		List<SimpleLocationInfo> locations = locationAssembler.deserialize(serializedEntity
				.getJSONArray(LOCATIONS_PARAM));

		return new RoutingResult(locations);
	}

}
