package com.shadowolf.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class Exceptions {
	public static String logInfo(Throwable e) {
		final StringBuilder info = new StringBuilder();
		final Writer trace = new StringWriter();
		final PrintWriter pWriter = new PrintWriter(trace);
		e.printStackTrace(pWriter);
		
		info.append("Exception: " + e.getClass() + "\n");
		info.append("Message: " + e.getMessage() + "\n");
		info.append("Stack trace: \n" + trace.toString());
		
		if(e.getCause() != null) {
			info.append("CAUSE: " + "\n");
			info.append(logInfo(e.getCause()));
		}
		
		return info.toString();
	}
}
