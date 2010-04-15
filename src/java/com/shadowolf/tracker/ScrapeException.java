package com.shadowolf.tracker;

@SuppressWarnings("serial")
public class ScrapeException extends Exception {
	public ScrapeException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public ScrapeException(final String message) {
		super(message);
	}
}
