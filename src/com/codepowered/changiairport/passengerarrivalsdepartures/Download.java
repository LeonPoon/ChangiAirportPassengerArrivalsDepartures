package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.text.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.util.Log;

public abstract class Download<DataType> {

	public static enum FlightsDate {
		today, tomorrow, yesterday;
	}

	public static abstract class AbstractDownload<DataType> extends Download<DataType> {

		private static final boolean IS_PRINT_DOWNLOADED = true;
		private final String url, typ;

		protected AbstractDownload(String url, FlightsDate date, String lang) {
			this.typ = url;
			this.url = "http://www.changiairport.com/cag-web/flights/" + url + "?date=" + date + "&lang=" + lang
					+ "&callback=JSON_CALLBACK";
			Log.i(typ + "_url", this.getUrl());
		}

		public abstract DataType readJsonReal(JSONObject json, Resources resources, String packageName)
				throws JSONException, ParseException;

		public DataType readJson(JSONObject json, Resources resources, String packageName)
				throws JSONException, ParseException {

			DataType data = readJsonReal(json, resources, packageName);
			if (IS_PRINT_DOWNLOADED)
				Log.d(typ + "_data", data == null ? "null" : data.toString());
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
	}

	public static abstract class FlightInfoDownload extends AbstractDownload<FlightInfo> {

		protected FlightInfoDownload(String url, FlightsDate date, String lang) {
			super(url, date, lang);
		}

		@Override
		public FlightInfo readJsonReal(JSONObject json, Resources resources, String packageName)
				throws JSONException, ParseException {
			return FlightInfo.parse(json, resources, packageName);
		}
	}

	public static class FlightArrivalsDownload extends FlightInfoDownload {

		protected FlightArrivalsDownload(String lang) {
			super("arrivals", FlightsDate.today, lang);
		}

		@Override
		public void setData(DataTarget target, FlightInfo data) {
			target.setFlightArrivalsInfo(data);
		}

	}

	public static class FlightDeparturesDownload extends FlightInfoDownload {

		protected FlightDeparturesDownload(String lang) {
			super("departures", FlightsDate.today, lang);
		}

		@Override
		public void setData(DataTarget target, FlightInfo data) {
			target.setFlightDeparturesInfo(data);
		}
	}

	public abstract String getUrl();

	public abstract DataType readJson(JSONObject json, Resources resources, String packageName)
			throws JSONException, ParseException;

	public abstract void setData(DataTarget target, DataType data);

}
