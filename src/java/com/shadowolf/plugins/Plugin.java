package com.shadowolf.plugins;

import org.xml.sax.Attributes;

import com.shadowolf.tracker.TrackerRequest.Event;


abstract class Plugin implements Runnable {
	public static enum Type {
		periodicThread
	}
	
	private final Type type;
	
	public Plugin(Type t, Attributes attributes) {
		this.type = t;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public boolean needsAnnounce() {
		return false;
	}
	
	public void doAnnounce(Event e, long uploaded, long downloaded, String passkey) {}
}
