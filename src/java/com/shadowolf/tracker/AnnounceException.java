package com.shadowolf.tracker;

@SuppressWarnings("serial")
public class AnnounceException extends Exception {
	
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
