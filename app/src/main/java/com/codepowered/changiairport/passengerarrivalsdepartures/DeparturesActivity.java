package com.codepowered.changiairport.passengerarrivalsdepartures;

import android.os.Bundle;
import android.util.Log;

public class DeparturesActivity extends AbstractFragment implements
		DeparturesFlightInfoSubscriber {
	@Override
	protected int getViewResource() {
		return R.layout.activity_departures;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setFlightDeparturesInfo(getMainActivity().getDeparturesFlightInfo()
				.addSubscriber(this));
	}

	@Override
	public void setFlightDeparturesInfo(FlightInfo flightInfo) {
		Log.d(toString(), "setFlightDeparturesInfo");
		super.setFlightInfo(flightInfo);
	}

	@Override
	protected int getAirportSelectionDescription() {
		return R.string.select_departure_to;
	}

	@Override
	public void onDestroyView() {
		MainActivity mainActivity = getMainActivity();
		mainActivity.getDeparturesFlightInfo().removeSubscriber(this);
		super.onDestroyView();
	}

	@Override
	protected int getFromToViaFormat() {
		return R.string.departureToVia;
	}

	@Override
	protected int getFromToFormat() {
		return R.string.departureTo;
	}

	@Override
	protected int getListLayoutResource() {
		return R.layout.layout_departures;
	}

	@Override
	protected String getOverseas(Flight item) {
		return item.getDestination();
	}
}
