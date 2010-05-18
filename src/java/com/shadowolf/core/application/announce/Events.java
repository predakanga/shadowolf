package com.shadowolf.core.application.announce;

/**
 * Enum wrapping all possible events that occur within an announce.
 * By having this specified as an enum, we don't need to create a bunch of 
 * strings all over the place.
 */
public enum Events {
	/**
	 * Client sent the "completed" event.
	 */
	COMPLETED,
	
	/**
	 * Client sent the "stopped" event.
	 */
	STOPPED,
	
	/**
	 * Client sent the "started" event.
	 */
	STARTED,
	
	/**
	 * Client did not send an event or sent an empty string; this is the default event.
	 */
	ANNOUNCE
}
