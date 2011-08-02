package com.shadowolf.plugin.points;

import com.shadowolf.protocol.Announce;

public interface AsyncAnnounceTask {
	public void run(Announce announce);
}
