package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;

public class Flight implements Comparable<Flight> {

	public static class Comparator implements java.util.Comparator<Flight> {

		public static final Comparator INSTANCE = new Comparator();

		@Override
		public int compare(Flight lhs, Flight rhs) {
			return lhs.parent == rhs.parent ? lhs.id - rhs.id : 0;
		}
	}

	@SuppressWarnings("serial")
	@SuppressLint("SimpleDateFormat")
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd") {
		{
			setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
		}
	};
	@SuppressWarnings("serial")
	@SuppressLint("SimpleDateFormat")
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm") {
		{
			setTimeZone(DATE_FORMAT.getTimeZone());
		}
	};

	private final Calendar date, scheduled_time, estimated_time;
	private final String airline, flight_no;
	private final String airport;
	private final String destination; // departure
	private final String origin; // arrival
	private final String via;
	private final String terminal;
	private final String belt; // arrival
	private final String checkin_row; // departure
	private final String gate; // departure
	private final String status;
	private final Calendar estimated_date;
	private final String airline_name, airline_alias;
	private final Date unixtime;
	private final String master_flight_no;
	private final String[] slave_flight_no;

	private final int id;
	private final FlightInfo parent;

	public static Calendar combineDateTime(Calendar d, Calendar t, TimeZone tz) {
		if (d == null || t == null)
			return null;
		final Calendar c = Calendar.getInstance(tz);
		c.set(d.get(Calendar.YEAR), d.get(Calendar.MONDAY),
				d.get(Calendar.DAY_OF_MONTH), t.get(Calendar.HOUR_OF_DAY),
				t.get(Calendar.MINUTE));
		return c;
	}

	public Flight(int id, FlightInfo parent, JSONObject json,
			Resources resources, String packageName) throws JSONException,
			ParseException {

		this.id = id;
		this.parent = parent;

		date = parseCalendar(json, "date");
		scheduled_time = parseCalendar(json, "scheduled_time", TIME_FORMAT);
		estimated_time = parseCalendar(json, "estimated_time", TIME_FORMAT);
		airline = getString(json, "airline");
		flight_no = getString(json, "flight_no");
		airport = getString(json, "airport");
		destination = parseString(json, resources, packageName, "airport",
				"destination");
		origin = parseString(json, resources, packageName, "airport", "origin");
		via = parseString(json, resources, packageName, "airport", "via");
		terminal = getString(json, "terminal");
		belt = getString(json, "belt");
		checkin_row = getString(json, "checkin_row");
		gate = getString(json, "gate");
		status = parseString(json, resources, packageName, "status", "status");
		estimated_date = parseCalendar(json, "estimated_date");
		airline_name = parseString(json, resources, packageName,
				"airline_name", "airline_name");
		airline_alias = getString(json, "airline_alias");
		unixtime = parseDate(json, "unixtime");
		master_flight_no = getString(json, "master_flight_no");
		JSONArray jsonArray = json.has("slave_flight_no") ? json
				.getJSONArray("slave_flight_no") : null;
		slave_flight_no = new String[jsonArray == null ? 0 : jsonArray.length()];
		for (int i = slave_flight_no.length; i-- > 0;)
			slave_flight_no[i] = jsonArray.getString(i);
	}

	public static Flight parse(int id, FlightInfo parent, JSONObject json,
			Resources resources, String packageName) throws JSONException,
			ParseException {
		return new Flight(id, parent, json, resources, packageName);
	}

	public static Calendar parseCalendar(JSONObject json, String keyName)
			throws JSONException, ParseException {
		return parseCalendar(json, keyName, DATE_FORMAT);
	}

	public static Calendar parseCalendar(JSONObject json, String keyName,
			DateFormat df) throws JSONException, ParseException {
		String val = getString(json, keyName);
		if (val == null || (val = val.trim()).isEmpty()
				|| "0000-00-00".equals(val))
			return null;
		Calendar cal = Calendar.getInstance(df.getTimeZone());
		cal.setTimeInMillis(df.parse(val).getTime());
		return cal;
	}

	public static String parseString(JSONObject json, Resources resources,
			String packageName, String itemType, String keyName)
			throws JSONException {
		String val = getString(json, keyName);
		if (val != null)
			val = translate(resources, packageName, itemType, val);
		return val;
	}

	public static String getString(JSONObject json, String keyName)
			throws JSONException {
		return json.has(keyName) ? json.getString(keyName) : null;
	}

	public static Date parseDate(JSONObject json, String keyName)
			throws JSONException {
		String val = getString(json, keyName);
		return val == null || (val = val.trim()).isEmpty() ? null : new Date(
				Long.parseLong(val));
	}

	public String getAirline() {
		return airline;
	}

	public String getAirline_alias() {
		return airline_alias;
	}

	public String getAirline_name() {
		return airline_name;
	}

	public String getAirport() {
		return airport;
	}

	public String getBelt() {
		return belt;
	}

	public String getCheckin_row() {
		return checkin_row;
	}

	public Calendar getDate() {
		return date;
	}

	public String getDestination() {
		return destination;
	}

	public Calendar getEstimated_date() {
		return estimated_date;
	}

	public Calendar getEstimated_time() {
		return estimated_time;
	}

	public String getFlight_no() {
		return flight_no;
	}

	public String getGate() {
		return gate;
	}

	public String getMaster_flight_no() {
		return master_flight_no;
	}

	public String getOrigin() {
		return origin;
	}

	public Calendar getScheduled_time() {
		return scheduled_time;
	}

	public String[] getSlave_flight_no() {
		return slave_flight_no;
	}

	public String getStatus() {
		return status;
	}

	public String getTerminal() {
		return terminal;
	}

	public Date getUnixtime() {
		return unixtime;
	}

	public String getVia() {
		return via;
	}

	public static String translate(Resources resources, String packageName,
			String itemType, String val) {
		int id = resources.getIdentifier(itemType + "/" + val, "string",
				packageName);
		if (id != 0)
			try {
				val = resources.getString(id);
			} catch (NotFoundException e) {
				// ignore
			}
		return val;
	}

	@Override
	public int compareTo(Flight another) {
		return Comparator.INSTANCE.compare(this, another);
	}
}
