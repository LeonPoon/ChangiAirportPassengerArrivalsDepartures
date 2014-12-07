package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.util.Iterator;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;

@SuppressWarnings("serial")
public abstract class AbstractMapCache<JsonType, ValueType> extends
		TreeMap<String, ValueType> {

	public AbstractMapCache(JSONObject json, Resources resources,
			String packageName) throws JSONException {

		for (Iterator<?> it = json.keys(); it.hasNext();) {
			String k = (String) it.next();
			JsonType val = getJSONObject(json, k);
			put(k, convert(val, resources, packageName));
		}
	}

	protected abstract JsonType getJSONObject(JSONObject json, String k)
			throws JSONException;

	protected abstract ValueType convert(JsonType val, Resources resources,
			String packageName) throws JSONException;

}
