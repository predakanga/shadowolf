package com.shadowolf.plugin;

/**
 * Exception representing a circular dependency.
 */
public class UnmetDependencyException extends Exception {
	private static final long serialVersionUID = -4647630999164525905L;

	public UnmetDependencyException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnmetDependencyException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnmetDependencyException(String message) {
		super(message);
	}

	public UnmetDependencyException(Throwable cause) {
		super(cause);
	}

	public UnmetDependencyException() {
	}

}
