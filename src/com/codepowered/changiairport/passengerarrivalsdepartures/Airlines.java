package com.codepowered.changiairport.passengerarrivalsdepartures;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;

public class Airlines extends AbstractMapCache<JSONObject, Airline> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3900874116745076702L;

	public Airlines(JSONObject json, Resources resources, String packageName)
			throws JSONException {
		super(json, resources, packageName);
	}

	public static Airlines parse(JSONObject json, Resources resources,
			String packageName) throws JSONException {
		return new Airlines(json, resources, packageName);
	}

	@Override
	protected Airline convert(JSONObject val, Resources resources,
			String packageName) throws JSONException {
		return new Airline(Flight.translate(resources, packageName, "airline",
				val.getString("name")), val.getString("website"));
	}

	@Override
	protected JSONObject getJSONObject(JSONObject json, String k)
			throws JSONException {
		return json.getJSONObject(k);
	}

}
