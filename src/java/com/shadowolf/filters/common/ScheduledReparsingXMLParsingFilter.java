package com.shadowolf.filters.common;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

abstract public class ScheduledReparsingXMLParsingFilter extends XMLParsingFilter {
	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

	@Override
	public void destroy() {
		this.executor.shutdown();
	}
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		super.init(config);
		
		this.executor.scheduleAtFixedRate(new ParserThread(), 0L, 600L, TimeUnit.SECONDS);
	}
	
	protected class ParserThread implements Runnable {
		@Override
		public void run() {
			if(isModified()) {
				parse();
			}
		}
	}
}
