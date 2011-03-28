package pl.edu.agh.jsonrpc;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public abstract class JSONRPCClient {

	protected static final String RESULT_KEY = "result";
	protected static final String PARAMS_KEY = "params";
	protected static final String METHOD_KEY = "method";
	protected static final String ERROR_KEY = "error";

	protected int soTimeout = 0, connectionTimeout = 0;
	
	protected abstract JSONObject doJSONRequest(JSONObject request) throws JSONRPCException;

	protected JSONObject doRequest(String method, Object[] params) throws JSONRPCException {
		// Copy method arguments in a json array
		JSONArray jsonParams = new JSONArray();
		for (int i = 0; i < params.length; i++) {
			jsonParams.add(params[i]);
		}

		// Create the json request object
		JSONObject jsonRequest = new JSONObject();
		try {
			jsonRequest.put(METHOD_KEY, method);
			jsonRequest.put(PARAMS_KEY, jsonParams);
		} catch (JSONException e1) {
			throw new JSONRPCException("Invalid JSON request", e1);
		}
		return doJSONRequest(jsonRequest);
	}

	/**
	 * Get the socket operation timeout in milliseconds
	 */
	public int getSoTimeout() {
		return soTimeout;
	}

	/**
	 * Set the socket operation timeout
	 * 
	 * @param soTimeout
	 *            timeout in milliseconds
	 */
	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	/**
	 * Get the connection timeout in milliseconds
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * Set the connection timeout
	 * 
	 * @param connectionTimeout
	 *            timeout in milliseconds
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public Object call(String method, Object... params) throws JSONRPCException {
		try {
			return doRequest(method, params).get(RESULT_KEY);
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a String
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public String callString(String method, Object... params) throws JSONRPCException {
		try {
			return doRequest(method, params).getString(RESULT_KEY);
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to String", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as an int
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public int callInt(String method, Object... params) throws JSONRPCException {
		try {
			return doRequest(method, params).getInt(RESULT_KEY);
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to int", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a long
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public long callLong(String method, Object... params) throws JSONRPCException {
		try {
			return doRequest(method, params).getLong(RESULT_KEY);
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to long", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a boolean
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public boolean callBoolean(String method, Object... params) throws JSONRPCException {
		try {
			return doRequest(method, params).getBoolean(RESULT_KEY);
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to boolean", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a double
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public double callDouble(String method, Object... params) throws JSONRPCException {
		try {
			return doRequest(method, params).getDouble(RESULT_KEY);
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to double", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a JSONObject
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public JSONObject callJSONObject(String method, Object... params) throws JSONRPCException {
		try {
			return doRequest(method, params).getJSONObject(RESULT_KEY);
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to JSONObject", e);
		}
	}

	/**
	 * Perform a remote JSON-RPC method call
	 * 
	 * @param method
	 *            The name of the method to invoke
	 * @param params
	 *            Arguments of the method
	 * @return The result of the RPC as a JSONArray
	 * @throws JSONRPCException
	 *             if an error is encountered during JSON-RPC method call
	 */
	public JSONArray callJSONArray(String method, Object... params) throws JSONRPCException {
		try {
			return doRequest(method, params).getJSONArray(RESULT_KEY);
		} catch (JSONException e) {
			throw new JSONRPCException("Cannot convert result to JSONArray", e);
		}
	}
}
