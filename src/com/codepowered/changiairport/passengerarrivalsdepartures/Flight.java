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
import android.util.Log;

public class Flight implements Comparable<Flight> {

	public static class Slave {
		private final String flight_no;
		private final String airline_name;
		private final String airline; // code

		public Slave(String flight_no, String airline_name, String airline) {
			this.flight_no = flight_no;
			this.airline = airline;
			this.airline_name = airline_name;
		}

		public String getAirline() {
			return airline;
		}

		public String getAirline_name() {
			return airline_name;
		}

		public String getFlight_no() {
			return flight_no;
		}
	}

	public static class Comparator implements java.util.Comparator<Flight> {

		public static final Comparator INSTANCE = new Comparator();

		@Override
		public int compare(Flight lhs, Flight rhs) {
			return lhs.parent == rhs.parent ? lhs.id - rhs.id : 0;
		}
	}

	@SuppressWarnings("serial")
	@SuppressLint("SimpleDateFormat")
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd") {
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
	@SuppressWarnings("serial")
	@SuppressLint("SimpleDateFormat")
	private static final DateFormat ESTIMATE_DATE_FORMAT = new SimpleDateFormat("dd MM") {
		{
			setTimeZone(DATE_FORMAT.getTimeZone());
		}
	};
	@SuppressWarnings("serial")
	@SuppressLint("SimpleDateFormat")
	private static final DateFormat SCHEDULED_DATETIME_FORMAT = new SimpleDateFormat("yyyyMMdd HH:mm:ss") {
		{
			setTimeZone(DATE_FORMAT.getTimeZone());
		}
	};

	private static final long HALF_YEAR_MILLIS = 60l * 60 * 24 * 180 * 1000;

	private final Calendar scheduled_time, estimated_time;
	private final String airline, flight_no;
	private final String airport;
	private final String destination; // departure
	private final String origin; // arrival
	private final String[] via;
	private final String terminal;
	private final String belt; // arrival
	private final String checkin_row; // departure
	private final String gate; // departure
	private final String status;
	private final String estimated_date;
	private final Calendar scheduled_datetime;
	private final String airline_name, airline_alias;
	private final Slave[] slave_flight_no;

	private final int id;
	private final FlightInfo parent;

	public static Calendar combineDateTime(Calendar d, Calendar t, TimeZone tz) {
		if (d == null || t == null)
			return null;
		final Calendar c = Calendar.getInstance(tz);
		c.set(d.get(Calendar.YEAR), d.get(Calendar.MONDAY), d.get(Calendar.DAY_OF_MONTH), t.get(Calendar.HOUR_OF_DAY),
				t.get(Calendar.MINUTE));
		return c;
	}

	public Flight(int id, FlightInfo parent, JSONObject json, Resources resources, String packageName)
			throws JSONException, ParseException {

		this.id = id;
		this.parent = parent;

		scheduled_time = parseCalendar(json, "scheduledTime", TIME_FORMAT);

		estimated_time = json.getBoolean("estimationTimeFlag") ? parseCalendar(json, "estimatedTime", TIME_FORMAT)
				: null;

		airline = getString(json, "airlineCode");
		flight_no = getString(json, "flightNo");
		airport = getString(json, "airportCode");
		destination = parseString(json, resources, packageName, "airport", "to");
		origin = parseString(json, resources, packageName, "airport", "from");
		JSONArray viaArray = json.has("via") ? json.getJSONArray("via") : null;
		via = new String[viaArray == null ? 0 : viaArray.length()];
		for (int i = via.length; i-- > 0;)
			via[i] = viaArray.getString(i);
		// via = parseString(json, resources, packageName, "airport", "via");
		terminal = getString(json, "terminal");
		belt = getString(json, "belt");
		checkin_row = getString(json, "checkInRow");
		gate = getString(json, "gate");
		status = parseString(json, resources, packageName, "status", "status");

		scheduled_datetime = parseCalendar(json, "scheduledDatetime", SCHEDULED_DATETIME_FORMAT);
		estimated_date = getString(json, "estimatedDate");
		airline_name = parseString(json, resources, packageName, "airline_name", "airlineDesc");
		airline_alias = getString(json, "airline_alias");
		JSONArray jsonArray = json.has("slaves") ? json.getJSONArray("slaves") : null;
		slave_flight_no = new Slave[jsonArray == null ? 0 : jsonArray.length()];
		for (int i = slave_flight_no.length; i-- > 0;) {
			JSONObject o = jsonArray.getJSONObject(i);
			slave_flight_no[i] = new Slave(getString(o, "flightNo"),
					parseString(o, resources, packageName, "airline_name", "airlineDesc"), getString(o, "airlineCode"));
		}
	}

	public static Flight parse(int id, FlightInfo parent, JSONObject json, Resources resources, String packageName)
			throws JSONException, ParseException {
		return new Flight(id, parent, json, resources, packageName);
	}

	public static Calendar parseCalendar(JSONObject json, String keyName) throws JSONException, ParseException {
		return parseCalendar(json, keyName, DATE_FORMAT);
	}

	public static Calendar parseCalendar(JSONObject json, String keyName, DateFormat df)
			throws JSONException, ParseException {
		String val = getString(json, keyName);
		if (val == null || (val = val.trim()).isEmpty() || "0000-00-00".equals(val))
			return null;
		Calendar cal = Calendar.getInstance(df.getTimeZone());
		cal.setTimeInMillis(df.parse(val).getTime());
		return cal;
	}

	public static Calendar parseCalendar(JSONObject json, String keyName, DateFormat df, Calendar dateTime)
			throws JSONException, ParseException {
		Calendar cal = parseCalendar(json, keyName, df);
		cal.set(Calendar.YEAR, dateTime.get(Calendar.YEAR));
		if (Math.abs(dateTime.getTimeInMillis() - cal.getTimeInMillis()) > HALF_YEAR_MILLIS)
			if (dateTime.get(Calendar.MONTH) == Calendar.JANUARY)
				cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);

		return cal;
	}

	public static String parseString(JSONObject json, Resources resources, String packageName, String itemType,
			String keyName) throws JSONException {
		String val = getString(json, keyName);
		if (val != null)
			val = translate(resources, packageName, itemType, val);
		return val;
	}

	public static String getString(JSONObject json, String keyName) throws JSONException {
		return json.has(keyName) ? json.getString(keyName) : null;
	}

	public static Date parseDate(JSONObject json, String keyName) throws JSONException {
		String val = getString(json, keyName);
		return val == null || (val = val.trim()).isEmpty() ? null : new Date(Long.parseLong(val));
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

	public String getDestination() {
		return destination;
	}

	public String getEstimated_date() {
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

	public String getOrigin() {
		return origin;
	}

	public Calendar getScheduled_datetime() {
		return scheduled_datetime;
	}

	public Calendar getScheduled_time() {
		return scheduled_time;
	}

	public Slave[] getSlave_flight_no() {
		return slave_flight_no;
	}

	public String getStatus() {
		return status;
	}

	public String getTerminal() {
		return terminal;
	}

	public String[] getVia() {
		return via;
	}

	public static String translate(Resources resources, String packageName, String itemType, String val) {
		String idStr = itemType + "." + (val == null ? "" : val.replaceAll("[^A-Za-z0-9_]", "_"));
		int id = resources.getIdentifier(idStr, "string", packageName);
		if (id == 0)
			Log.i("translation", idStr);
		else
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
