package com.shadowolf.plugins;

import com.shadowolf.announce.Announce;
import com.shadowolf.tracker.AnnounceException;

public interface AnnounceAction {
	//PMD is complaining about the public keyword here... really?!
	public void doAnnounce(Announce announce) throws AnnounceException;
}
