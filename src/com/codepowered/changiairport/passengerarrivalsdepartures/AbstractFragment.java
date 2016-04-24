package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.codepowered.changiairport.passengerarrivalsdepartures.Flight.Slave;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public abstract class AbstractFragment extends Fragment
		implements AirlinesSubscriber, AirportsSubscriber, OnItemSelectedListener {

	private MainActivity mainActivity;
	private Airlines airlines;
	private Airports airports;
	private FlightInfo flightInfo;

	private ArrayAdapterUpdater<Entry<String, String>, Airport> spinnerAdapter;
	private ArrayAdapterUpdater<Flight, Flight> listAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainActivity = (MainActivity) getActivity();
	}

	public MainActivity getMainActivity() {
		return mainActivity;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(toString(), "onStart");
	}

	protected abstract int getViewResource();

	@Override
	public void onDestroyView() {
		MainActivity mainActivity = getMainActivity();
		mainActivity.getAirlines().removeSubscriber(this);
		mainActivity.getAirports().removeSubscriber(this);
		super.onDestroyView();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(toString(), "onCreateView");
		// Inflate the layout for this fragment
		View v = inflater.inflate(getViewResource(), container, false);
		setupSpinner(getSpinner(v), inflater);
		setupListView(getListView(v), inflater);
		return v;
	}

	private ListView getListView(View v) {
		return (ListView) v.findViewById(R.id.listView1);
	}

	protected abstract int getAirportSelectionDescription();

	private Spinner getSpinner(View v) {
		// Log.d(toString(), "getSpinner(" + v + ")");
		return (Spinner) v.findViewById(R.id.spinner1);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(toString(), "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		MainActivity mainActivity = getMainActivity();
		setAirlines(mainActivity.getAirlines().addSubscriber(this));
		setAirports(mainActivity.getAirports().addSubscriber(this));
	}

	public Airlines getAirlines() {
		return airlines;
	}

	@Override
	public void setAirlines(Airlines airlines) {
		Log.d(toString(), "setAirlines");
		this.airlines = airlines;
	}

	public Airports getAirports() {
		return airports;
	}

	@Override
	public void setAirports(Airports airports) {
		Log.d(toString(), "setAirports");
		this.airports = airports;
		getMainActivity().runOnUiThread(spinnerAdapter);
	}

	public void setFlightInfo(FlightInfo flightInfo) {
		Log.d(toString(), "setFlightInfo");
		this.flightInfo = flightInfo;
		getMainActivity().runOnUiThread(listAdapter);
	}

	public FlightInfo getFlightInfo() {
		return flightInfo;
	}

	private void setupListView(ListView lv, final LayoutInflater inflater) {

		listAdapter = new ArrayAdapterUpdater<Flight, Flight>(null, null,
				new ArrayAdapter<Flight>(getMainActivity(), getListLayoutResource()) {

					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View rowView = convertView;

						if (rowView == null) {
							// Get a new instance of the row layout view
							rowView = (LinearLayout) inflater.inflate(R.layout.layout_arrivals, parent, false);
						}

						/** Set data to your Views. */
						Flight item = getItem(position);

						String overseas = getOverseas(item);
						String[] via = item.getVia();
						if (via == null || via.length == 0)
							overseas = String.format(getText(getFromToFormat()).toString(), overseas);
						else
							overseas = String.format(getText(getFromToViaFormat()).toString(), overseas,
									deepToString(via));

						((TextView) rowView.findViewById(R.id.txtAirport)).setText(overseas);

						((TextView) rowView.findViewById(R.id.txtScheduledTime))
								.setText(formatDateTime(item.getScheduled_datetime()));
						((TextView) rowView.findViewById(R.id.txtEstimatedTime))
								.setText(formatDateTime(item.getEstimated_date(), item.getEstimated_time()) + " "
										+ item.getStatus());

						LinearLayout flights = (LinearLayout) rowView.findViewById(R.id.flightsLayout);
						flights.removeAllViews();

						TextView mainFlight = (TextView) inflater.inflate(R.layout.layout_aircode_main, flights, false);
						String flightNo = item.getFlight_no();
						mainFlight.setText(getAirlineName(item.getAirline_name()) + flightNo);
						flights.addView(mainFlight);

						for (Slave slave : item.getSlave_flight_no()) {
							TextView subFlight = (TextView) inflater.inflate(R.layout.layout_aircode_sub, flights,
									false);
							subFlight.setText(getAirlineName(slave.getAirline_name()) + slave.getFlight_no());
							flights.addView(subFlight);
						}

						return rowView;
					}

				}) {

			@Override
			protected boolean shouldSelect(Flight item) {
				Spinner spinner = getSpinner(AbstractFragment.this.getView());
				Airport selection = (Airport) spinner.getSelectedItem();
				return selection == null || selection.includes(item);
			}

			@Override
			protected Collection<Flight> getRawObjects() {
				return flightInfo;
			}

			@Override
			protected Flight convert(Flight raw) {
				return raw;
			}
		};
		lv.setAdapter(listAdapter.getArrayAdapter());
	}

	protected String deepToString(Object[] via) {
		String sep = ", ";
		StringBuilder b = null;
		for (Object o : via)
			if (b == null)
				b = new StringBuilder("" + o);
			else
				b.append(sep).append(o);
		return b.toString();
	}

	protected abstract String getOverseas(Flight item);

	protected abstract int getListLayoutResource();

	protected String getAirlineName(String airlineDesc) {
		return airlineDesc == null || (airlineDesc = airlineDesc.trim()).isEmpty() ? "" : (airlineDesc + " ");
	}

	protected abstract int getFromToViaFormat();

	protected abstract int getFromToFormat();

	protected CharSequence formatDateTime(String d, Calendar t) {
		if (t == null)
			return "-";
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
		df.setTimeZone(t.getTimeZone());
		Date c = new Date(t.getTimeInMillis());
		return d == null || (d = d.trim()).isEmpty() ? df.format(c) : (df.format(c) + " (" + d + ")");
	}

	protected CharSequence formatDateTime(Calendar d, Calendar t) {
		Calendar c = d == null && t == null ? null : Flight.combineDateTime(d, t, TimeZone.getDefault());
		return formatDateTime(c);
	}

	protected CharSequence formatDateTime(Calendar c) {
		if (c == null)
			return "-";
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
		df.setTimeZone(c.getTimeZone());
		return df.format(new Date(c.getTimeInMillis()));
	}

	private void setupSpinner(Spinner spinner, final LayoutInflater inflater) {

		spinnerAdapter = new ArrayAdapterUpdater<Entry<String, String>, Airport>(Airport.Comparator.INSTANCE,
				Airport.EMPTY_ENTRY, new ArrayAdapter<Airport>(getMainActivity(), R.layout.dropdown_picked_airport) {
					{
						setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					}

					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View rowView = convertView;

						if (rowView == null) {
							// Get a new instance of the row layout view
							rowView = (LinearLayout) inflater.inflate(R.layout.dropdown_picked_airport, parent, false);

						}

						/** Set data to your Views. */
						Airport item = getItem(position);
						((TextView) rowView.findViewById(R.id.textViewSelectDescription))
								.setText(getAirportSelectionDescription());
						((TextView) rowView.findViewById(R.id.textViewAirportName)).setText(item.getName());

						return rowView;
					}

				}) {

			@Override
			protected Airport convert(Entry<String, String> raw) {
				return new Airport(raw);
			}

			@Override
			protected Collection<Entry<String, String>> getRawObjects() {
				Airports airports = AbstractFragment.this.airports;
				return airports == null ? null : airports.entrySet();
			}
		};

		spinner.setAdapter(spinnerAdapter.getArrayAdapter());

		spinner.setOnItemSelectedListener(this);
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		listAdapter.run();
	}

	public void onNothingSelected(AdapterView<?> parent) {
		listAdapter.run();
	}
}
