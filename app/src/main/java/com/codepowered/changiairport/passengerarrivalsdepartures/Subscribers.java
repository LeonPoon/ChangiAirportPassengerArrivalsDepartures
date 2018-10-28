package com.codepowered.changiairport.passengerarrivalsdepartures;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Subscribers<DataType, SubscriberType> {

	private volatile DataType data;
	private final CopyOnWriteArrayList<SubscriberType> subscribers = new CopyOnWriteArrayList<SubscriberType>();

	public DataType getData() {
		return data;
	}

	public void setData(DataType data) {
		this.data = data;
		for (SubscriberType subscriber : subscribers)
			setData(data, subscriber);
	}

	public DataType addSubscriber(SubscriberType subscriber) {
		subscribers.add(subscriber);
		return data;
	}

	public void removeSubscriber(SubscriberType subscriber) {
		subscribers.remove(subscriber);
	}

	protected abstract void setData(DataType data, SubscriberType subscriber);
}
