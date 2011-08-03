package com.shadowolf.plugin.points;

import com.shadowolf.protocol.Announce;

public interface AnnounceDecorator extends AbstractPlugin {
	public Announce decorate(Announce announce);
}
