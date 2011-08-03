package com.shadowolf.plugin.points;

import java.util.concurrent.TimeUnit;

public interface ScheduledTask extends Runnable, AbstractPlugin {
	public TimeUnit getTimeUnit();
	public int getInterval();
	public int getDelay();
	public void run();
}
