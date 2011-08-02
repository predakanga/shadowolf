package com.shadowolf.plugin.points;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;

public abstract class ScheduledTask {
	protected TimeUnit timeUnit;
	protected int interval;
	
	@GuardedBy("this")
	private boolean running;
	
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
	
	public int getInterval() {
		return interval;
	}
	
	public void run() {
		synchronized(this) {
			if(running) {
				//log this?
				return;
			} else {
				running = true;
			}
		}
		
		this.execute();
	}
	
	public abstract void execute();
}
