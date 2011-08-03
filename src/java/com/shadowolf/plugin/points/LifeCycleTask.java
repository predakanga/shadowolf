package com.shadowolf.plugin.points;

public interface LifeCycleTask extends AbstractPlugin {
	public void onStart();
	public void onShutdown();
}
