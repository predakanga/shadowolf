package com.shadowolf.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that contains only static methods for dealing 
 * with exceptions.  It's preferable that exceptions that get
 * logged, get logged with this class.
 */
public class Exceptions {
	private static final Logger LOG = LoggerFactory.getLogger(Exceptions.class);
	private Exceptions() {
		
	}
	
	/**
	 * Takes a Throwable and turns it into a nicely formatted String containing,
	 *  recursively, all the info needed to log it.
	 * @param exception A Throwable to turn into a String for logging purposes
	 * @return A string fit for logging exceptions with complete information about where it came from, recursively
	 */
	public static String logInfo(Throwable exception) {
		StringBuilder info = new StringBuilder();
		Writer trace = new StringWriter();
		PrintWriter pWriter = new PrintWriter(trace);
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
	
	/**
	 * Logs a Throwable using the output format from {@link #logInfo(Throwable)}.
	 * 
	 * It's preferred that all exception logging be done from this class
	 * so that they can all be handled uniformly in the logging mechanism.
	 * @param exception the Throwable to log
	 */
	public static void log(Throwable exception) {
		LOG.error(logInfo(exception));
	}
}
