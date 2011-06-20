package pl.edu.agh.skeleton;

import static pl.edu.agh.service.TrafficService.CALCULATE_ROUTE_METHOD;
import static pl.edu.agh.service.TrafficService.GET_TRAFFIC_DATA_METHOD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.agh.assembler.RoutingResultJSONAssembler;
import pl.edu.agh.assembler.SimpleLocationInfoJSONAssembler;
import pl.edu.agh.assembler.TrafficDataJSONAssembler;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.jsonrpc.JSONRPCSkeleton;
import pl.edu.agh.model.SimpleLocationInfo;
import pl.edu.agh.service.TrafficService;

@Component(TrafficSkeleton.BEAN_NAME)
public class TrafficSkeleton extends JSONRPCSkeleton {

	public final static String BEAN_NAME = "trafficSkeleton";

	@Autowired
	private TrafficService trafficService;

	@Autowired
	private SimpleLocationInfoJSONAssembler simpleLocationInfoJSONAssembler;

	@Autowired
	private TrafficDataJSONAssembler trafficDataJSONAssembler;

	@Autowired
	private RoutingResultJSONAssembler routingResultJSONAssembler;

	@Override
	protected JSONObject invoke(String methodName, JSONArray params) throws JSONException, JSONRPCException,
			NoSuchMethodException {
		if (methodName.equals(GET_TRAFFIC_DATA_METHOD)) {
			return invokeGetTrafficData(params);
		} else if (methodName.equals(CALCULATE_ROUTE_METHOD)) {
			return invokeCalculateRoute(params);
		} else {
			throw new NoSuchMethodException("Missing method: " + methodName + " in trafficService");
		}

	}

	private JSONObject invokeCalculateRoute(JSONArray params) throws JSONException, JSONRPCException {
		SimpleLocationInfo start = simpleLocationInfoJSONAssembler.deserialize(params.getJSONObject(0));
		SimpleLocationInfo end = simpleLocationInfoJSONAssembler.deserialize(params.getJSONObject(1));
		boolean useTrafficDataToRoute = params.getBoolean(2);

		return routingResultJSONAssembler.serialize(trafficService.calculateRoute(start, end, useTrafficDataToRoute));
	}

	private JSONObject invokeGetTrafficData(JSONArray params) throws JSONException, JSONRPCException {
		SimpleLocationInfo location = simpleLocationInfoJSONAssembler.deserialize(params.getJSONObject(0));
		double radius = params.getDouble(1);
		return trafficDataJSONAssembler.serialize(trafficService.getTrafficData(location, radius));
	}

}
