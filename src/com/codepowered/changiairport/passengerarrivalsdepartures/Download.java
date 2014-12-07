package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.text.ParseException;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.util.Log;

public abstract class Download<DataType> {

	public static abstract class AbstractDownload<DataType> extends
			Download<DataType> {

		private static final boolean IS_PRINT_DOWNLOADED = false;
		private final String url;
		private final Pattern callbackPattern;

		protected AbstractDownload(String url, String callback) {
			this.url = "http://202.136.9.40/webfids/fidsp/" + url;
			callbackPattern = Pattern.compile("^\\s*" + Pattern.quote(callback)
					+ "\\s*\\(\\s*(.*)\\s*\\)\\s*;\\s*$", Pattern.DOTALL);
		}

		public abstract DataType readJsonReal(JSONObject json,
				Resources resources, String packageName) throws JSONException,
				ParseException;

		public DataType readJson(JSONObject json, Resources resources,
				String packageName) throws JSONException, ParseException {

			DataType data = readJsonReal(json, resources, packageName);
			if (IS_PRINT_DOWNLOADED)
				Log.d(getClass().getName(),
						data == null ? "null" : data.toString());
			return data;
		}

		@Override
		public String getUrl() {
			return url;
		}

		@Override
		public String toString() {
			return getUrl();
		}

		@Override
		public Pattern getCallback() {
			return callbackPattern;
		}
	}

	public static class AirportsDownload extends AbstractDownload<Airports> {

		protected AirportsDownload() {
			super("get_airport_cache.php", "airport_callback");
		}

		@Override
		public Airports readJsonReal(JSONObject json, Resources resources,
				String packageName) throws JSONException {
			return Airports.parse(json, resources, packageName);
		}

		@Override
		public void setData(DataTarget target, Airports data) {
			target.setAirports(data);
		}

	}

	public static class AirlinesDownload extends AbstractDownload<Airlines> {

		protected AirlinesDownload() {
			super("get_airline_cache.php", "airline_callback");
		}

		@Override
		public Airlines readJsonReal(JSONObject json, Resources resources,
				String packageName) throws JSONException {
			return Airlines.parse(json, resources, packageName);
		}

		@Override
		public void setData(DataTarget target, Airlines data) {
			target.setAirlines(data);
		}

	}

	public abstract static class FlightInfoDownload extends
			AbstractDownload<FlightInfo> {

		protected FlightInfoDownload(String url) {
			super("get_flightinfo_cache.php?d=0&type=" + url,
					"flightinfo_callback");
		}

		@Override
		public FlightInfo readJsonReal(JSONObject json, Resources resources,
				String packageName) throws JSONException, ParseException {
			return FlightInfo.parse(json, resources, packageName);
		}

	}

	public static class FlightArrivalsDownload extends FlightInfoDownload {

		protected FlightArrivalsDownload() {
			super("pa");
		}

		@Override
		public void setData(DataTarget target, FlightInfo data) {
			target.setFlightArrivalsInfo(data);
		}

	}

	public static class FlightDeparturesDownload extends FlightInfoDownload {

		protected FlightDeparturesDownload() {
			super("pd");
		}

		@Override
		public void setData(DataTarget target, FlightInfo data) {
			target.setFlightDeparturesInfo(data);
		}
	}

	public abstract String getUrl();

	public abstract DataType readJson(JSONObject json, Resources resources,
			String packageName) throws JSONException, ParseException;

	public abstract void setData(DataTarget target, DataType data);

	public abstract Pattern getCallback();
}
