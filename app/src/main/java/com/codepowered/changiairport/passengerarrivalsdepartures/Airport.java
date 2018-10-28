package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.util.Map.Entry;

public class Airport implements Comparable<Airport> {

	public static class Comparator implements java.util.Comparator<Airport> {

		public static final Comparator INSTANCE = new Comparator();

		@Override
		public int compare(Airport lhs, Airport rhs) {
			if (lhs == rhs)
				return 0;
			if (lhs == null)
				return -1;
			if (rhs == null)
				return 1;
			return lhs.getName().compareTo(rhs.getName());
		}
	}

	static final Airport EMPTY_ENTRY = new Airport(null) {

		public String getCode() {
			return "";
		}

		public String getName() {
			return "";
		}
	};

	private Entry<String, String> entry;

	public Airport(Entry<String, String> entry) {
		this.entry = entry;
	}

	public String getCode() {
		return entry.getKey();
	}

	public String getName() {
		return entry.getValue();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Airport))
			return false;
		Airport other = (Airport) o;
		return equals(getCode(), other.getCode())
				&& equals(getName(), other.getName());
	}

	private boolean equals(String s, String t) {
		return s == t || (s != null && s.equals(t));
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		return entry == null ? 0 : entry.hashCode();
	}

	@Override
	public int compareTo(Airport another) {
		return Comparator.INSTANCE.compare(this, another);
	}

	public boolean includes(Flight flight) {
		if (entry == null || getCode().equals(flight.getDestination()) || getCode().equals(flight.getOrigin()))
			return true;
		for (String via : flight.getVia())
			if (getCode().equals(via))
				return true;
		return false;
	}
}
