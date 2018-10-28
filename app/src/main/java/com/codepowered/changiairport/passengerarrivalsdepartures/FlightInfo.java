package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.text.ParseException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.codepowered.changiairport.passengerarrivalsdepartures.Download.FlightInfoDownload;

import android.content.res.Resources;

public class FlightInfo extends ArrayList<Flight> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4501401327146887572L;
	private final String dateTime;
	private final FlightInfoDownload<?> download;

	public FlightInfo(FlightInfoDownload<?> download, int len, String dateTime) {
		super(len);
		this.download = download;
		this.dateTime = dateTime;
	}

	public FlightInfoDownload<?> getDownload() {
		return download;
	}

	public String getDateTime() {
		return dateTime;
	}

	public static FlightInfo parse(FlightInfoDownload<?> download, JSONObject json, Resources resources,
			String packageName) throws JSONException, ParseException {

		String dateTime = json.getString("dateTime");

		JSONArray jsonArray = json.getJSONArray("carriers");
		int len = jsonArray.length();

		FlightInfo info = new FlightInfo(download, len, dateTime);

		for (int i = 0; i < len; i++) {
			json = jsonArray.getJSONObject(i);
			Flight flight = Flight.parse(i, info, json, resources, packageName);
			info.add(flight);
		}
		return info;
	}
}
