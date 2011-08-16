package pl.edu.agh.jsonrpc;

import java.io.IOException;

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
			
		} catch (IOException e) {
			throw new JSONRPCException(e.getMessage());
		}
		return new JSONObject();
	}
}
