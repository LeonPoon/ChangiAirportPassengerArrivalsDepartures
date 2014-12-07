package com.codepowered.changiairport.passengerarrivalsdepartures;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;

public class Airports extends AbstractMapCache<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8566581841744014433L;

	public Airports(JSONObject json, Resources resources, String packageName)
			throws JSONException {
		super(json, resources, packageName);
	}

	public static Airports parse(JSONObject json, Resources resources,
			String packageName) throws JSONException {
		return new Airports(json, resources, packageName);
	}

	@Override
	protected String convert(String val, Resources resources, String packageName) {
		if (val == null)
			return null;
		return Flight.translate(resources, packageName, "airport", val);
	}

	@Override
	protected String getJSONObject(JSONObject json, String k)
			throws JSONException {
		return json.getString(k);
	}

}
