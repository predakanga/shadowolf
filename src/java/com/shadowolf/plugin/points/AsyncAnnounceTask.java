package com.shadowolf.plugin.points;

import com.shadowolf.ShadowolfComponent;
import com.shadowolf.protocol.Announce;

public interface AsyncAnnounceTask extends ShadowolfComponent {
	public void run(Announce announce);
}
