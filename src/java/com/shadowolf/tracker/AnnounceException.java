package com.shadowolf.tracker;

@SuppressWarnings("serial")
public class AnnounceException extends Exception {
	public AnnounceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AnnounceException(final String message) {
		super(message);
	}
}
