package com.shadowolf.plugins;

import java.lang.reflect.Constructor;

public class PluginBean {
	private Constructor<Plugin> constructor;
	private String type;
	private int repeatInterval;
	private int startDelay;
	public Constructor<Plugin> getConstructor() {
		return constructor;
	}
	public void setConstructor(Constructor<Plugin> constructor) {
		this.constructor = constructor;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getRepeatInterval() {
		return repeatInterval;
	}
	public void setRepeatInterval(int repeatInterval) {
		this.repeatInterval = repeatInterval;
	}
	public void setStartDelay(int startDelay) {
		this.startDelay = startDelay;
	}
	public int getStartDelay() {
		return startDelay;
	}
}
