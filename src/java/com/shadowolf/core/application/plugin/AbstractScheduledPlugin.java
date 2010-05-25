package com.shadowolf.core.application.plugin;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class AbstractScheduledPlugin extends AbstractPlugin implements Runnable {
	public static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;
	public static final int DEFAULT_INITIAL_DELAY = 0;
	public static final int DEFAULT_PERIOD = 900;
	
	private final TimeUnit unit;
	private final int initialDelay;
	private final int period;
	
	public AbstractScheduledPlugin(Map<String, String> options) {
		if(options.get("unit") != null) {
			this.unit = this.parseUnit(options.get("unit"));
		} else {
			this.unit = DEFAULT_UNIT;
		}
		
		if(options.get("delay") != null) {
			this.initialDelay = Integer.parseInt(options.get("delay"));
		} else {
			this.initialDelay = DEFAULT_INITIAL_DELAY;
		}
		
		if(options.get("period") != null) {
			this.period = Integer.parseInt(options.get("period"));
		} else {
			this.period = DEFAULT_PERIOD;
		}
	}
	
	public final TimeUnit getUnit() {
		return this.unit;
	}

	public final int getInitialDelay() {
		return this.initialDelay;
	}

	public final int getPeriod() {
		return this.period;
	}

	private final TimeUnit parseUnit(String unit) {
		try{ 
			return TimeUnit.valueOf(unit.toUpperCase());
		} catch(IllegalArgumentException e) {
			return TimeUnit.SECONDS;
		}
	}
}
