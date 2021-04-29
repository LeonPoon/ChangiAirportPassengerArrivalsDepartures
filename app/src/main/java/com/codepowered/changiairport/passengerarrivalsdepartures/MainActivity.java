package com.codepowered.changiairport.passengerarrivalsdepartures;

import android.app.*;
import android.app.ActionBar.Tab;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.codepowered.changiairport.passengerarrivalsdepartures.Download.FlightArrivalsDownload;
import com.codepowered.changiairport.passengerarrivalsdepartures.Download.FlightDeparturesDownload;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Exchanger;

public class MainActivity extends Activity implements DataTarget, OnClickListener {

	private static final String TAG = "MainActivity";

	private final Subscribers<Airports, AirportsSubscriber> airports = new Subscribers<Airports, AirportsSubscriber>() {

		@Override
		protected void setData(Airports data, AirportsSubscriber subscriber) {
			subscriber.setAirports(data);
		}
	};

	private final Subscribers<Airlines, AirlinesSubscriber> airlines = new Subscribers<Airlines, AirlinesSubscriber>() {

		@Override
		protected void setData(Airlines data, AirlinesSubscriber subscriber) {
			subscriber.setAirlines(data);
		}
	};

	private final Subscribers<FlightInfo, ArrivalsFlightInfoSubscriber> arrivalsFlightInfo = new Subscribers<FlightInfo, ArrivalsFlightInfoSubscriber>() {

		@Override
		protected void setData(FlightInfo data, ArrivalsFlightInfoSubscriber subscriber) {
			subscriber.setFlightArrivalsInfo(data);
		}
	};

	private final Subscribers<FlightInfo, DeparturesFlightInfoSubscriber> departuresFlightInfo = new Subscribers<FlightInfo, DeparturesFlightInfoSubscriber>() {

		@Override
		protected void setData(FlightInfo data, DeparturesFlightInfoSubscriber subscriber) {
			subscriber.setFlightDeparturesInfo(data);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		addTab(actionBar, R.string.arriving, ArrivalsActivity.class);
		addTab(actionBar, R.string.departing, DeparturesActivity.class);

		startLoadingData();
	}

	public class DownloadTask extends AsyncTask<Download<?, ?>, String, Throwable> {

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(MainActivity.this, getText(R.string.loading), getText(R.string.pleaseWait));
		}

		@Override
		protected Throwable doInBackground(Download<?, ?>... downloads) {

			dialog.setMax(downloads.length);

			for (Download<?, ?> download : downloads)

				try {
					download(download);
				} catch (Throwable e) {
					return e;
				} finally {
					dialog.setProgress(dialog.getProgress() + 1);
				}

			return null;
		}

		@Override
		protected void onPostExecute(Throwable exception) {
			if (exception != null) {
				Log.e("download", exception.toString());
				new AlertDialog.Builder(MainActivity.this).setMessage(exception.toString())
						.setNeutralButton("OK", MainActivity.this).setTitle("Failed").show();
			}
			dialog.dismiss();
			finishedDownloadingData();
		}
	}

	private <T, U> void download(Download<T, U> download)
			throws InterruptedException, IOException, JSONException, ParseException {
		String url = download.getUrl();
		String input = readUrl(url);
		JSONObject json = new JSONObject(input);
		download.setData(this, download.readJson(json, getResources(), getPackageName()));
	}

	public String readUrl(String url) throws IOException, InterruptedException {
		final Exchanger<Object> exchanger = new Exchanger<>();
		StringBuilder builder = new StringBuilder();
		RequestQueue queue = Volley.newRequestQueue(this);
		Request stringRequest = new StringRequest(Request.Method.GET, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						try {
							exchanger.exchange(response);
						} catch (InterruptedException e) {
							Log.e(TAG, "onResponse: while exchanging", e);
						}
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				try {
					exchanger.exchange(error);
				} catch (InterruptedException e) {
					Log.e(TAG, "onErrorResponse: while exchanging", e);
				}
			}
		}) {
			@Override
			protected Response<String> parseNetworkResponse(NetworkResponse response) {
				String parsed;
				try {
					parsed = new String(response.data, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// Since minSdkVersion = 8, we can't call
					// new String(response.data, Charset.defaultCharset())
					// So suppress the warning instead.
					parsed = new String(response.data);
				}
				return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
			}
		};
		queue.add(stringRequest);
		Object o = exchanger.exchange(null);
		if (o instanceof Throwable)
			throw new IOException((Throwable)o);
		return o.toString();
	}

	private <A extends Fragment> void addTab(ActionBar actionBar, int strId, Class<A> aClass) {

		actionBar.addTab(
				actionBar.newTab().setText(getText(strId)).setTabListener(new TabListener<A>(this, null, aClass)));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startLoadingData();
			return true;
		} else if (id == R.id.action_about) {
			showAbout();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showAbout() {
		AboutDialogFragment dialog = new AboutDialogFragment();
		dialog.show(getFragmentManager(), "AboutDialogFragment");
	}

	public static enum Sources {
		arrivals, departures;
	}

	public class AirportsSynchroniser {

		private final EnumMap<Sources, FlightInfo> map = new EnumMap<Sources, FlightInfo>(Sources.class);

		public AirportsSynchroniser add(Sources source, FlightInfo flightInfo) {
			map.put(source, flightInfo);
			return map.size() == Sources.values().length ? this : null;
		}

		public Airports getAirports() {
			Map<String, String> m = new TreeMap<String, String>();
			for (FlightInfo flInf : map.values())
				for (Flight fl : flInf) {
					if (fl.getDestination() != null)
						m.put(fl.getDestination(), fl.getDestination());
					if (fl.getOrigin() != null)
						m.put(fl.getOrigin(), fl.getOrigin());
					for (String via : fl.getVia())
						m.put(via, via);
				}
			try {
				return Airports.parse(new JSONObject(m), getResources(), getPackageName());
			} catch (JSONException e) {
				return null;
			}
		}

		public FlightInfo getFlightInfo(Sources source) {
			return map.get(source);
		}
	}

	private void startLoadingData() {
		String lang = getText(R.string.lang).toString();
		AirportsSynchroniser sync = new AirportsSynchroniser();
		Download<?, ?>[] downloads = new Download[] { new FlightArrivalsDownload<AirportsSynchroniser>(lang, sync),
				new FlightDeparturesDownload<AirportsSynchroniser>(lang, sync) };
		new DownloadTask().execute(downloads);
	}

	private void finishedDownloadingData() {
		// TODO Auto-generated method stub

	}

	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {

		private Fragment mFragment;
		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;

		/**
		 * Constructor used each time a new tab is created.
		 * 
		 * @param activity
		 *            The host Activity, used to instantiate the fragment
		 * @param tag
		 *            The identifier tag for the fragment
		 * @param clz
		 *            The fragment's Class, used to instantiate the fragment
		 */
		public TabListener(Activity activity, String tag, Class<T> clz) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
		}

		/* The following are each of the ActionBar.TabListener callbacks */

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// Check if the fragment is already initialized
			if (mFragment == null) {
				// If not, instantiate and add it to the activity
				mFragment = Fragment.instantiate(mActivity, mClass.getName());
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				// If it exists, simply attach it in order to show it
				ft.attach(mFragment);
			}
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null) {
				// Detach the fragment, because another one is being attached
				ft.detach(mFragment);
			}
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// User selected the already selected tab. Usually do nothing.
		}
	}

	public Subscribers<Airlines, AirlinesSubscriber> getAirlines() {
		return airlines;
	}

	public Subscribers<Airports, AirportsSubscriber> getAirports() {
		return airports;
	}

	public Subscribers<FlightInfo, ArrivalsFlightInfoSubscriber> getArrivalsFlightInfo() {
		return arrivalsFlightInfo;
	}

	public Subscribers<FlightInfo, DeparturesFlightInfoSubscriber> getDeparturesFlightInfo() {
		return departuresFlightInfo;
	}

	@Override
	public void setAirports(Airports airports) {
		for (String code : getResources().getStringArray(R.array.extra_airports))
			airports.addExtra(code, getResources(), getPackageName());
		this.airports.setData(airports);
	}

	@Override
	public void setAirlines(Airlines airlines) {
		this.airlines.setData(airlines);
	}

	@Override
	public void setFlightArrivalsInfo(FlightInfo flightInfo) {
		setFlightInfo(flightInfo, Sources.arrivals);
	}

	@Override
	public void setFlightDeparturesInfo(FlightInfo flightInfo) {
		setFlightInfo(flightInfo, Sources.departures);
	}

	private void setFlightInfo(FlightInfo flightInfo, Sources source) {
		AirportsSynchroniser sync = (AirportsSynchroniser) flightInfo.getDownload().getCallbackObject();
		sync = sync.add(source, flightInfo);
		if (sync != null) {
			Airports airports = sync.getAirports();
			if (airports != null)
				setAirports(airports);
			this.arrivalsFlightInfo.setData(sync.getFlightInfo(Sources.arrivals));
			this.departuresFlightInfo.setData(sync.getFlightInfo(Sources.departures));
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		finish();
	}
}
