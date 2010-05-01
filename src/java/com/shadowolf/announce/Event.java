package com.shadowolf.announce;

public enum Event {
	STARTED("started"), 
	STOPPED("stopped"), 
	ANNOUNCE("announce"), 
	COMPLETED("completed");

	private String humanReadable;

	private Event(String humanReadable) {
		this.humanReadable = humanReadable;
	}

	public String toString() {
		return this.humanReadable;
	}
}
