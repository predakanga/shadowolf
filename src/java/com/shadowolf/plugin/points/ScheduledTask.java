package com.shadowolf.plugin.points;

import java.util.concurrent.TimeUnit;

public interface ScheduledTask {
	public TimeUnit getTimeUnit();
	public int getInterval();
	public void run();
}
