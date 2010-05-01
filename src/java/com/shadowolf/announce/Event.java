package com.shadowolf.announce;

public enum Event {
	STARTED("started"), 
	STOPPED("stopped"), 
	ANNOUNCE("announce"), 
	COMPLETED("completed");

	private final String humanReadable;

	private Event(final String humanReadable) {
		this.humanReadable = humanReadable;
	}

	public String toString() {
		return this.humanReadable;
	}
}
