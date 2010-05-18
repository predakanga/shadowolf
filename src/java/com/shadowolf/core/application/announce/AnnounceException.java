package com.shadowolf.core.application.announce;

import com.shadowolf.core.application.tracker.response.Errors;

/**
 * Special purpose exception.  This is NOT to be caught anywhere, except in the servlets
 * that talk to the client directly, as their messages are used as client output.  Because
 * the message is sent directly to the client, the message needs to be bencoded.
 */
public class AnnounceException extends Exception {
	private static final long serialVersionUID = 3807391211560609242L;

	public AnnounceException(Errors e) {
		this(e.toString());
	}
	
	public AnnounceException(Errors e, final Throwable cause) {
		this(e.toString(), cause);
	}
	
	public AnnounceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AnnounceException(final String message) {
		super(message);
	}
}
