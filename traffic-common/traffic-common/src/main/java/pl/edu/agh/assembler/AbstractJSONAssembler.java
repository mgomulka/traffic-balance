package pl.edu.agh.assembler;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJSONAssembler<E> {

	public abstract JSONObject serialize(E entity) throws JSONException;

	public JSONArray serialize(List<E> entities) throws JSONException {
		JSONArray serializedEntities = new JSONArray();
		for (E entity : entities) {
			serializedEntities.put(serialize(entity));
		}

		return serializedEntities;
	}

	public abstract E deserialize(JSONObject serializedEntity) throws JSONException;

	public List<E> deserialize(JSONArray serializedEntities) throws JSONException {
		List<E> enities = new ArrayList<E>();

		for (int i = 0; i < serializedEntities.length(); i++) {
			enities.add(deserialize(serializedEntities.getJSONObject(i)));
		}

		return enities;
	}
}
