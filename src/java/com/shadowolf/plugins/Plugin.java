package com.shadowolf.plugins;

import org.xml.sax.Attributes;

abstract class Plugin implements Runnable {
	public static enum Type {
		periodicThread,
		filter
	}
	
	private final Type type;
	
	public Plugin(Type t, Attributes attributes) {
		this.type = t;
	}
	
	public Type getType() {
		return this.type;
	}
	
}
