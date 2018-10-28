package com.codepowered.changiairport.passengerarrivalsdepartures;

import android.os.Bundle;
import android.util.Log;

public class ArrivalsActivity extends AbstractFragment implements
		ArrivalsFlightInfoSubscriber {

	@Override
	protected int getViewResource() {
		return R.layout.activity_arrivals;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setFlightArrivalsInfo(getMainActivity().getArrivalsFlightInfo()
				.addSubscriber(this));
	}

	@Override
	public void setFlightArrivalsInfo(FlightInfo flightInfo) {
		Log.d(toString(), "setFlightArrivalsInfo");
		super.setFlightInfo(flightInfo);
	}

	@Override
	protected int getAirportSelectionDescription() {
		return R.string.select_arrival_from;
	}

	@Override
	public void onDestroyView() {
		MainActivity mainActivity = getMainActivity();
		mainActivity.getArrivalsFlightInfo().removeSubscriber(this);
		super.onDestroyView();
	}

	@Override
	protected int getFromToViaFormat() {
		return R.string.arrivalFromVia;
	}

	@Override
	protected int getFromToFormat() {
		return R.string.arrivalFrom;
	}

	@Override
	protected int getListLayoutResource() {
		return R.layout.layout_arrivals;
	}

	@Override
	protected String getOverseas(Flight item) {
		return item.getOrigin();
	}

}
