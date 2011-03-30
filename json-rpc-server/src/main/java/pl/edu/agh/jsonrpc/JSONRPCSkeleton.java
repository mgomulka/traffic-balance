package pl.edu.agh.jsonrpc;

import java.io.BufferedReader;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class JSONRPCSkeleton {

	protected abstract JSONObject invoke(String methodName, JSONArray params) throws Exception;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	protected static final JSONObject NULL_RESULT;
	static {
		NULL_RESULT = new JSONObject();
		try {
			NULL_RESULT.put("NO_RESULT", JSONObject.NULL);
		} catch (JSONException e) {}
	}

	void processRequest(BufferedReader in, PrintWriter out) {
		JSONObject jsonResponse = new JSONObject();
		try {
			JSONObject jsonRequest = new JSONObject(in.readLine());
			
			String methodName = jsonRequest.getString(JSONRPCConstants.METHOD_KEY);
			JSONArray params = jsonRequest.getJSONArray(JSONRPCConstants.PARAMS_KEY);
			
			JSONObject result = invoke(methodName, params);
			
			jsonResponse.put(JSONRPCConstants.RESULT_KEY, result);
		} catch (Exception ex) {
			try {
				jsonResponse.put(JSONRPCConstants.ERROR_KEY, ex.getMessage());
			} catch (JSONException e) {
				log.error("Error during creating response", ex);
			}
		}
		
		out.print(jsonResponse.toString());
	}
	
	

}
