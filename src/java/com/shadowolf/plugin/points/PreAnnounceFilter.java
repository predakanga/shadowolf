package com.shadowolf.plugin.points;

import com.shadowolf.protocol.Announce;

public interface PreAnnounceFilter {
	public boolean filter(Announce announce);
}
