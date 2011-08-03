package com.shadowolf.plugin.points;

import com.shadowolf.protocol.Announce;

public interface PreAnnounceFilter extends AbstractPlugin {
	public boolean filter(Announce announce);
}
