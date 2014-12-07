package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepowered.changiairport.passengerarrivalsdepartures.Download.AirlinesDownload;
import com.codepowered.changiairport.passengerarrivalsdepartures.Download.AirportsDownload;
import com.codepowered.changiairport.passengerarrivalsdepartures.Download.FlightArrivalsDownload;
import com.codepowered.changiairport.passengerarrivalsdepartures.Download.FlightDeparturesDownload;

public class MainActivity extends Activity implements DataTarget,
		OnClickListener {

	private static final Download<?>[] DOWNLOADS = new Download[] {
			new AirlinesDownload(), new AirportsDownload(),
			new FlightArrivalsDownload(), new FlightDeparturesDownload() };

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
		protected void setData(FlightInfo data,
				ArrivalsFlightInfoSubscriber subscriber) {
			subscriber.setFlightArrivalsInfo(data);
		}
	};

	private final Subscribers<FlightInfo, DeparturesFlightInfoSubscriber> departuresFlightInfo = new Subscribers<FlightInfo, DeparturesFlightInfoSubscriber>() {

		@Override
		protected void setData(FlightInfo data,
				DeparturesFlightInfoSubscriber subscriber) {
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

	public class DownloadTask extends AsyncTask<Download<?>, String, Throwable> {

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(MainActivity.this,
					getText(R.string.loading), getText(R.string.pleaseWait));
		}

		@Override
		protected Throwable doInBackground(Download<?>... downloads) {

			dialog.setMax(downloads.length);

			for (Download<?> download : downloads)

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
				new AlertDialog.Builder(MainActivity.this)
						.setMessage(exception.toString())
						.setNeutralButton("OK", MainActivity.this)
						.setTitle("Failed").show();
			}
			dialog.dismiss();
			finishedDownloadingData();
		}
	}

	private <T> void download(Download<T> download)
			throws ClientProtocolException, IOException, JSONException,
			ParseException {
		String url = download.getUrl();
		String input = readUrl(url);
		JSONObject json = new JSONObject(stripCallback(input, download));
		download.setData(this,
				download.readJson(json, getResources(), getPackageName()));
	}

	private String stripCallback(String input, Download<?> download) {
		Pattern cb = download.getCallback();
		try {
			Matcher matcher = cb.matcher(input);
			if (matcher.matches())
				return matcher.group(1);
			throw new IllegalStateException(cb.pattern() + ": " + input);
		} catch (IllegalStateException e) {
			throw new IllegalStateException(cb.pattern() + ": " + input, e);
		}
	}

	public String readUrl(String url) throws ClientProtocolException,
			IOException {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = client.execute(httpGet);
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			content = new GZIPInputStream(content);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					content));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} else {
			throw new IOException("status = " + statusCode);
		}
		return builder.toString();
	}

	private <A extends Fragment> void addTab(ActionBar actionBar, int strId,
			Class<A> aClass) {

		actionBar.addTab(actionBar.newTab().setText(getText(strId))
				.setTabListener(new TabListener<A>(this, null, aClass)));
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
		}
		return super.onOptionsItemSelected(item);
	}

	private void startLoadingData() {
		new DownloadTask().execute(DOWNLOADS);
	}

	private void finishedDownloadingData() {
		// TODO Auto-generated method stub

	}

	public static class TabListener<T extends Fragment> implements
			ActionBar.TabListener {

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
		this.airports.setData(airports);
	}

	@Override
	public void setAirlines(Airlines airlines) {
		this.airlines.setData(airlines);
	}

	@Override
	public void setFlightArrivalsInfo(FlightInfo flightInfo) {
		this.arrivalsFlightInfo.setData(flightInfo);
	}

	@Override
	public void setFlightDeparturesInfo(FlightInfo flightInfo) {
		this.departuresFlightInfo.setData(flightInfo);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		finish();
	}
}
