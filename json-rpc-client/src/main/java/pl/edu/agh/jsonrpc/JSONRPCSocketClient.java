package pl.edu.agh.jsonrpc;

import org.json.JSONObject;

public class JSONRPCSocketClient extends JSONRPCClient {

	private final RawDataSocket socket;
	
	public JSONRPCSocketClient(RawDataSocket socket) {
		this.socket = socket;
	}
	
	/**
	 * this method does not wait for response!
	 */
	@Override
	protected JSONObject doJSONRequest(JSONObject request) throws JSONRPCException {
		try {

			String data = request.toString();
			socket.sendData(data.getBytes());
			
			JSONObject result = new JSONObject();
			JSONObject resultOK = new JSONObject();
			resultOK.put("result", "OK");
			result.put(JSONRPCConstants.RESULT_KEY, resultOK);
			
			return result;
		} catch (Exception e) {
			throw new JSONRPCException(e.getMessage());
		}
	}
}
