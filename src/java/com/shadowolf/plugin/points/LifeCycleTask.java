package com.shadowolf.plugin.points;

import com.shadowolf.ShadowolfComponent;

public interface LifeCycleTask extends ShadowolfComponent {
	public void onStart();
	public void onShutdown();
}
