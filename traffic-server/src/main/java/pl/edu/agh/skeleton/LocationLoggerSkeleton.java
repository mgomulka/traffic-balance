package pl.edu.agh.skeleton;

import static pl.edu.agh.service.LocationLoggerService.SEND_LOCATION_DATA_METHOD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.agh.assembler.LocationDataJSONAssembler;
import pl.edu.agh.assembler.LocationInfoJSONAssembler;
import pl.edu.agh.jsonrpc.JSONRPCException;
import pl.edu.agh.jsonrpc.JSONRPCSkeleton;
import pl.edu.agh.model.LocationData;
import pl.edu.agh.model.LocationInfo;
import pl.edu.agh.service.LocationLoggerService;

@Component(LocationLoggerSkeleton.BEAN_NAME)
public class LocationLoggerSkeleton extends JSONRPCSkeleton {

	public final static String BEAN_NAME = "locationLoggerSkeleton";

	@Autowired
	private LocationLoggerService locationLoggerService;

	@Autowired
	private LocationDataJSONAssembler locationDataJSONAssembler;

	@Autowired
	private LocationInfoJSONAssembler locationInfoJSONAssembler;

	@Override
	protected JSONObject invoke(String methodName, JSONArray params) throws JSONException, JSONRPCException,
			NoSuchMethodException {
		if (methodName.equals(SEND_LOCATION_DATA_METHOD)) {
			return invokeSendLocationData(params);
		} else {
			throw new NoSuchMethodException("Missing method: " + methodName + " in locationLoggerService");
		}

	}

	private JSONObject invokeSendLocationData(JSONArray params) throws JSONRPCException, JSONException {
		LocationData locationData = locationDataJSONAssembler.deserialize(params.getJSONObject(0));
		for (LocationInfo info : locationData.getLocationInfos()) {
			System.out.println(locationInfoJSONAssembler.serialize(info));
		}

		locationLoggerService.sendLocationData(locationData);

		return NULL_RESULT;
	}

}
