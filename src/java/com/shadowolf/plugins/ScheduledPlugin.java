package com.shadowolf.plugins;

import java.util.concurrent.TimeUnit;

import org.xml.sax.Attributes;

//PMD wants abstract classes to be called AbstractXXX... rubbish.
abstract public class ScheduledPlugin extends Plugin { // NOPMD by Eddie on 3/20/10 3:11 AM
	public final static int DEFAULT_DELAY = 15;
	public final static int DEFAULT_PERIOD = 15;
	public final static TimeUnit DEFAULT_UNIT = TimeUnit.MINUTES;
	
	private int initialDelay = DEFAULT_DELAY;
	private int period = DEFAULT_PERIOD;
	private TimeUnit unit = DEFAULT_UNIT;
	
	public ScheduledPlugin(final Attributes attributes) {
		super(Type.periodicThread);
		
		if(attributes.getValue("unit") != null) {
			final String unit = attributes.getValue("unit");
			
			if("minutes".equals(unit)) {
				this.setUnit(TimeUnit.MINUTES);
			} else if ("seconds".equals(unit)) {
				this.setUnit(TimeUnit.SECONDS);
			} else if ("hours".equals(unit)) {
				this.setUnit(TimeUnit.HOURS);
			} else if ("days".equals(unit)) {
				this.setUnit(TimeUnit.DAYS);
			}
		}
		
		if(attributes.getValue("delay") != null) {
			this.setInitialDelay(Integer.parseInt(attributes.getValue("delay")));
		}
		
		if(attributes.getValue("period") != null) {
			this.setInitialDelay(Integer.parseInt(attributes.getValue("period")));
		}
	}

	public int getInitialDelay() {
		return initialDelay;
	}

	public int getPeriod() {
		return period;
	}

	public TimeUnit getUnit() {
		return unit;
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