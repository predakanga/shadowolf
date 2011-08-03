package com.shadowolf.plugin.points;

import com.shadowolf.ShadowolfComponent;
import com.shadowolf.protocol.Announce;

public interface PreAnnounceFilter extends ShadowolfComponent {
	public boolean filter(Announce announce);
}
