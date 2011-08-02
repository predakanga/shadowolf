package com.shadowolf.plugin;

/**
 * Exception representing a circular dependency.
 */
public class CircularDependencyException extends Exception {
	private static final long serialVersionUID = -4647630999164525905L;

	public CircularDependencyException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CircularDependencyException(String message, Throwable cause) {
		super(message, cause);
	}

	public CircularDependencyException(String message) {
		super(message);
	}

	public CircularDependencyException(Throwable cause) {
		super(cause);
	}

	public CircularDependencyException() {
	}

}
