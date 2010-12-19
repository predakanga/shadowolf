package com.shadowolf.errorhandling;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.log4j.Logger;

public aspect ExceptionLog {
	private static final Logger LOG = Logger.getLogger(ExceptionLog.class);
	
	before(Throwable t) : handler(Throwable+) && args(t) {
		LOG.error(logInfo(t));
	}
	
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
