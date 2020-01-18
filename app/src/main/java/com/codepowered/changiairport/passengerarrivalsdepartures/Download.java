package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.text.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.util.Log;

public abstract class Download<DataType, CallbackType> {

	public static enum FlightsDate {
		today, tomorrow, yesterday;
	}

	public static abstract class AbstractDownload<DataType, CallbackType> extends Download<DataType, CallbackType> {

		private static final boolean IS_PRINT_DOWNLOADED = true;
		private final String url, typ;
		private final CallbackType callbackObject;

		protected AbstractDownload(String url, FlightsDate date, String lang, CallbackType callbackObject) {
			this.callbackObject = callbackObject;
			this.typ = url;
			this.url = "https://www.changiairport.com/cag-web/flights/" + url + "?date=" + date + "&lang=" + lang
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

		public CallbackType getCallbackObject() {
			return callbackObject;
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

	public static abstract class FlightInfoDownload<CallbackType> extends AbstractDownload<FlightInfo, CallbackType> {

		protected FlightInfoDownload(String url, FlightsDate date, String lang, CallbackType callbackObject) {
			super(url, date, lang, callbackObject);
		}

		@Override
		public FlightInfo readJsonReal(JSONObject json, Resources resources, String packageName)
				throws JSONException, ParseException {
			return FlightInfo.parse(this, json, resources, packageName);
		}
	}

	public static class FlightArrivalsDownload<CallbackType> extends FlightInfoDownload<CallbackType> {

		protected FlightArrivalsDownload(String lang, CallbackType callbackObject) {
			super("arrivals", FlightsDate.today, lang, callbackObject);
		}

		@Override
		public void setData(DataTarget target, FlightInfo data) {
			target.setFlightArrivalsInfo(data);
		}

	}

	public static class FlightDeparturesDownload<CallbackType> extends FlightInfoDownload<CallbackType> {

		protected FlightDeparturesDownload(String lang, CallbackType callbackObject) {
			super("departures", FlightsDate.today, lang, callbackObject);
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
