package com.shadowolf.plugins;

import com.shadowolf.announce.Announce;
import com.shadowolf.tracker.AnnounceException;

public interface AnnounceFilter {
	//PMD is complaining about the public keyword here... really?!
	public void filterAnnounce(Announce announce) throws AnnounceException;
}
