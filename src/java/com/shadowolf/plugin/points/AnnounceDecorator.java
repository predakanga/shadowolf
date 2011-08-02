package com.shadowolf.plugin.points;

import com.shadowolf.protocol.Announce;

public interface AnnounceDecorator {
	public Announce decorate(Announce announce);
}
