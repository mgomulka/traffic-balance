package pl.edu.agh.android.messageComposer.impl;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.edu.agh.android.messageComposer.MessageComposer;
import android.location.Location;
import android.util.Log;

public class JSONMessageComposer implements MessageComposer {

	@Override
	public String composeLocationDataMessage(List<Location> locations) {

		JSONArray array = new JSONArray();
		try {
			for(Location loc : locations) {
			
				JSONObject object = new JSONObject();
					
				object.put("lon", loc.getLongitude());
				object.put("lat", loc.getLatitude());
				object.put("time", loc.getTime());
				object.put("speed", loc.getSpeed());
				object.put("acc", loc.getAccuracy());
				object.put("course", loc.getBearing());
				
				array.put(object);
			}
		} catch(JSONException e) {
			
			Log.e("JSONComposer", e.toString());
		}
		return array.toString();
	}

}
