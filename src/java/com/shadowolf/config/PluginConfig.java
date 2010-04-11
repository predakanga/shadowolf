package com.shadowolf.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class PluginConfig {
	private final Logger LOGGER = Logger.getLogger(PluginConfig.class);
	private final String className;

	private int period = 15;
	private int delay = 10;

	private TimeUnit timeUnit = TimeUnit.MINUTES;
	private final HashMap<String, String> parameters = new HashMap<String, String>();

	public PluginConfig(final String className) {
		this.className = className;

	}

	public void setTimeUnit(final String unit) {
		if("seconds".equalsIgnoreCase(unit)) {
			this.timeUnit = TimeUnit.SECONDS;
		} else if ("minutes".equalsIgnoreCase(unit)) {
			this.timeUnit = TimeUnit.MINUTES;
		} else if ("hours".equalsIgnoreCase(unit)) {
			this.timeUnit = TimeUnit.HOURS;
		} else {
			this.LOGGER.warn("Could not parse proper value: " + unit);
		}

	}

	public void setPeriod(final String period) {
		this.period = Integer.parseInt(period);
	}

	public void setDelay(final String delay) {
		this.delay = Integer.parseInt(delay);
	}

	public void setParameter(final String name, final String value) {
		this.parameters.put(name, value);
	}

	public Map<String, String> getParameters() {
		return this.parameters;
	}

	public String getClassName() {
		return this.className;
	}

	public int getPeriod() {
		return this.period;
	}

	public int getDelay() {
		return this.delay;
	}

	public TimeUnit getTimeUnit() {
		return this.timeUnit;
	}
}