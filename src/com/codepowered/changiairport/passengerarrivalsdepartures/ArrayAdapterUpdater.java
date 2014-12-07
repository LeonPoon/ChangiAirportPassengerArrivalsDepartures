package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.widget.ArrayAdapter;

public abstract class ArrayAdapterUpdater<RawType, ConvertType> implements
		Runnable {

	private final Comparator<ConvertType> comparator;
	private final ConvertType emptyObject;
	private final ArrayAdapter<ConvertType> arrayAdapter;

	public ArrayAdapterUpdater(Comparator<ConvertType> comparator,
			ConvertType emptyObject, ArrayAdapter<ConvertType> arrayAdapter) {
		this.comparator = comparator;
		this.emptyObject = emptyObject;
		this.arrayAdapter = arrayAdapter;
	}

	@Override
	public void run() {

		ArrayAdapter<ConvertType> adapter = getArrayAdapter();

		Collection<RawType> raws = getRawObjects();
		if (raws == null)
			return;

		List<ConvertType> converted = new LinkedList<ConvertType>();

		ConvertType empty = getEmptyObject();
		if (empty != null)
			converted.add(empty);

		for (RawType raw : getRawObjects()) {
			ConvertType convert = convert(raw);
			if (shouldSelect(convert))
				converted.add(convert);
		}

		for (int i = adapter.getCount(); --i >= 0;) {
			ConvertType convertObj = adapter.getItem(i);
			if (!converted.contains(convertObj))
				adapter.remove(convertObj);
		}

		for (ConvertType convertObj : converted)
			if (adapter.getPosition(convertObj) < 0)
				adapter.add(convertObj);

		Comparator<ConvertType> comparator = getComparator();
		adapter.sort(comparator);
	}

	protected boolean shouldSelect(ConvertType convert) {
		return true;
	}

	protected Comparator<ConvertType> getComparator() {
		return comparator;
	}

	protected abstract ConvertType convert(RawType raw);

	protected ConvertType getEmptyObject() {
		return emptyObject;
	}

	protected abstract Collection<RawType> getRawObjects();

	protected ArrayAdapter<ConvertType> getArrayAdapter() {
		return arrayAdapter;
	}
}
