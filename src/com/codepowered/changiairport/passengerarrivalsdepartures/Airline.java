package com.codepowered.changiairport.passengerarrivalsdepartures;

public class Airline {

	private final String name;
	private final String website;

	public Airline(String name, String website) {
		this.name = name;
		this.website = website;
	}

	public String getName() {
		return name;
	}

	public String getWebsite() {
		return website;
	}
}
