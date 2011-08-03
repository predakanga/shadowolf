package com.shadowolf.plugin.points;

import com.shadowolf.protocol.Announce;

public interface AsyncAnnounceTask extends AbstractPlugin {
	public void run(Announce announce);
}
