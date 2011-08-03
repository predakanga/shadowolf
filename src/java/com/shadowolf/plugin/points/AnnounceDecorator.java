package com.shadowolf.plugin.points;

import com.shadowolf.ShadowolfComponent;
import com.shadowolf.protocol.Announce;

public interface AnnounceDecorator extends ShadowolfComponent {
	public Announce decorate(Announce announce);
}
