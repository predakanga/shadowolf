package com.shadowolf.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Class which provides a static method for uniform logging of exceptions
 */
final public class Exceptions {
	private Exceptions() {
	}
	
	/**
	 * Takes a Throwable and turns it into a nicely formatted String containing,
	 *  recursively, all the info needed to log it.
	 * @param exception A Throwable to turn into a String for logging purposes
	 * @return A string fit for logging exceptions with complete information about where it came from, recursively
	 */
	public static String logInfo(final Throwable exception) {
		final StringBuilder info = new StringBuilder();
		final Writer trace = new StringWriter();
		final PrintWriter pWriter = new PrintWriter(trace);
		exception.printStackTrace(pWriter);
		
		info.append("Exception: " + exception.getClass() + "\n");
		info.append("Message: " + exception.getMessage() + "\n");
		info.append("Stack trace: \n" + trace.toString());
		
		if(exception.getCause() != null) {
			info.append("CAUSE: " + "\n");
			info.append(logInfo(exception.getCause()));
		}
		
		return info.toString();
	}
}
