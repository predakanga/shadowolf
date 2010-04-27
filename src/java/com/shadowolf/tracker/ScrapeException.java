package com.shadowolf.tracker;

@SuppressWarnings("serial")
public class ScrapeException extends Exception {
	public ScrapeException(Errors e) {
		this(e.toString());
	}
	
	public ScrapeException(Errors e, final Throwable cause) {
		this(e.toString(), cause);
	}
	
	public ScrapeException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public ScrapeException(final String message) {
		super(message);
	}
}
