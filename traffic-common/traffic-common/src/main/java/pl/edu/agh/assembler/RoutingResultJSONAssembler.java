package pl.edu.agh.assembler;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.model.RoutingResult;
import pl.edu.agh.model.SimpleLocationInfo;

public class RoutingResultJSONAssembler extends AbstractJSONAssembler<RoutingResult> {

	private static final String LOCATIONS_PARAM = "locs";
	private static final String MP_PARAM = "mp"; //t

	private SimpleLocationInfoJSONAssembler locationAssembler;

	public RoutingResultJSONAssembler(SimpleLocationInfoJSONAssembler locationAssembler) {
		this.locationAssembler = locationAssembler;
	}

	@Override
	public JSONObject serialize(RoutingResult routingResult) throws JSONException {
		JSONObject serializedRoutingResult = new JSONObject();

		serializedRoutingResult.put(LOCATIONS_PARAM, locationAssembler.serialize(routingResult.getLocations()));
		if (routingResult.getMatchedPoints() != null) {
			serializedRoutingResult.put(MP_PARAM, locationAssembler.serialize(routingResult.getMatchedPoints())); //t
		}

		return serializedRoutingResult;
	}

	@Override
	public RoutingResult deserialize(JSONObject serializedEntity) throws JSONException {
		List<SimpleLocationInfo> locations = locationAssembler.deserialize(serializedEntity
				.getJSONArray(LOCATIONS_PARAM));
		List<SimpleLocationInfo> mp = null;//t
		if (serializedEntity.has(MP_PARAM)) {
			 mp = locationAssembler.deserialize(serializedEntity
					.getJSONArray(MP_PARAM));
		}

		RoutingResult routingResult = new RoutingResult(locations);
		routingResult.setMatchedPoints(mp);
		return routingResult;
	}

}
