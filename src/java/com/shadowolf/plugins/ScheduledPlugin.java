package com.shadowolf.plugins;

import java.util.concurrent.TimeUnit;


//PMD wants abstract classes to be called AbstractXXX... rubbish.
abstract public class ScheduledPlugin extends Plugin implements Runnable { // NOPMD by Eddie on 3/20/10 3:11 AM
	private int initialDelay;
	private int period;
	private TimeUnit unit;

	public int getInitialDelay() {
		return this.initialDelay;
	}

	public int getPeriod() {
		return this.period;
	}

	public TimeUnit getUnit() {
		return this.unit;
	}

	final public void setInitialDelay(final int initialDelay) {
		this.initialDelay = initialDelay;
	}

	final public void setPeriod(final int period) {
		this.period = period;
	}

	final public void setUnit(final TimeUnit unit) {
		this.unit = unit;
	}
}