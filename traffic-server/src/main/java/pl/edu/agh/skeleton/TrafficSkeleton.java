package pl.edu.agh.skeleton;

import static pl.edu.agh.assembler.LocationDataBatchJSONAssembler.deserialize;
import static pl.edu.agh.assembler.TrafficDataJSONAssembler.serialize;
import static pl.edu.agh.service.TrafficService.GET_TRAFFIC_DATA_METHOD;
import static pl.edu.agh.service.TrafficService.SEND_TRAFFIC_DATA_METHOD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.jsonrpc.JSONRPCSkeleton;
import pl.edu.agh.model.LocationDataBatch;
import pl.edu.agh.service.TrafficService;

@Component(TrafficSkeleton.BEAN_NAME)
public class TrafficSkeleton extends JSONRPCSkeleton {
	
	public final static String BEAN_NAME = "trafficSkeleton";
	
	@Autowired
	private TrafficService trafficService;

	@Override
	protected JSONObject invoke(String methodName, JSONArray params) throws JSONException, JSONRPCException, NoSuchMethodException {
		if (methodName.equals(GET_TRAFFIC_DATA_METHOD)) {
			return invokeGetTrafficData(params);
		} else if (methodName.equals(SEND_TRAFFIC_DATA_METHOD)) {
			return invokeSendTrafficData(params);
		} else {
			throw new NoSuchMethodException("Missing method: " + methodName + " in trafficService");
		}
		
	}

	private JSONObject invokeSendTrafficData(JSONArray params) throws JSONException, JSONRPCException {
		LocationDataBatch batch = deserialize(params.getJSONObject(0));
		
		trafficService.sendTrafficData(batch);
		
		return NULL_RESULT;
	}

	private JSONObject invokeGetTrafficData(JSONArray params) throws JSONException, JSONRPCException {
		double latitude = params.getDouble(0);
		double longitude = params.getDouble(1);
		
		return serialize(trafficService.getTrafficData(latitude, longitude));
	}

}
