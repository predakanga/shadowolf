package com.shadowolf.plugins;

//more PMD telling me how to name my things
abstract class Plugin implements Runnable { //NOPMD
	public static enum Type {
		periodicThread,
		filter
	}
	
	private final Type type; //NOPMD ... this isn't a bean, derp derp
	
	public Plugin(final Type type) {
		this.type = type;
	}
	
	public final Type getType() {
		return this.type;
	}
	
}
