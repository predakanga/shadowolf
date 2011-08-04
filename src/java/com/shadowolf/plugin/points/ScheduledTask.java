package com.shadowolf.plugin.points;

import java.util.concurrent.TimeUnit;

import com.shadowolf.ShadowolfComponent;

public interface ScheduledTask extends Runnable, ShadowolfComponent {
	public TimeUnit getTimeUnit();
	public int getInterval();
	public int getDelay();
	@Override
	public void run();
}
