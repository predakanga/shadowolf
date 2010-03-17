package com.shadowolf.plugins;

import java.util.concurrent.TimeUnit;

import org.xml.sax.Attributes;

abstract public class ScheduledPlugin extends Plugin {
	public final static int DEFAULT_DELAY = 15;
	public final static int DEFAULT_PERIOD = 15;
	public final static TimeUnit DEFAULT_UNIT = TimeUnit.MINUTES;
	
	private int initialDelay = DEFAULT_DELAY;
	private int period = DEFAULT_PERIOD;
	private TimeUnit unit = DEFAULT_UNIT;
	
	public ScheduledPlugin(Attributes attributes) {
		super(Type.periodicThread, attributes);
		
		if(attributes.getValue("unit") != null) {
			String unit = attributes.getValue("unit");
			
			if(unit.equals("minutes")) {
				this.setUnit(TimeUnit.MINUTES);
			} else if (unit.equals("seconds")) {
				this.setUnit(TimeUnit.SECONDS);
			} else if (unit.equals("hours")) {
				this.setUnit(TimeUnit.HOURS);
			} else if (unit.equals("days")) {
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

	public void setInitialDelay(int initialDelay) {
		this.initialDelay = initialDelay;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}
}