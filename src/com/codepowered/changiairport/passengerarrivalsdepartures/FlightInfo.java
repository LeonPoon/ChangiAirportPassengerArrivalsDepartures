package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;

public class FlightInfo extends ArrayList<Flight> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4501401327146887572L;
	private final Calendar yesterday, today, tomorrow;
	private final Date updatedAt;

	public FlightInfo(int len, Calendar yesterday, Calendar today,
			Calendar tomorrow, Date updatedAt) {
		super(len);
		this.yesterday = yesterday;
		this.today = today;
		this.tomorrow = tomorrow;
		this.updatedAt = updatedAt;
	}

	public Calendar getYesterday() {
		return yesterday;
	}

	public Calendar getToday() {
		return today;
	}

	public Calendar getTomorrow() {
		return tomorrow;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public static FlightInfo parse(JSONObject json, Resources resources,
			String packageName) throws JSONException, ParseException {

		long updatedAt = json.getLong("updatedat");
		Calendar yesterday = Flight.parseCalendar(json, "yesterday");
		Calendar today = Flight.parseCalendar(json, "today");
		Calendar tomorrow = Flight.parseCalendar(json, "tomorrow");

		JSONArray jsonArray = json.getJSONArray("flights");
		int len = jsonArray.length();

		FlightInfo info = new FlightInfo(len, yesterday, today, tomorrow,
				new Date(updatedAt));

		for (int i = 0; i < len; i++) {
			json = jsonArray.getJSONObject(i);
			Flight flight = Flight.parse(i, info, json, resources, packageName);
			if (flight.getMaster_flight_no().equals(flight.getFlight_no()))
				info.add(flight);
		}
		return info;
	}
}
