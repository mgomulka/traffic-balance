package pl.edu.agh.service;

import org.json.JSONObject;

public interface AsyncDataReceiver {

	long getRequestCode();
	void dataReceived(JSONObject object);
}
