package com.shadowolf.plugins;

import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerRequest.Event;

public interface AnnounceFilter {
	//PMD is complaining about the public keyword here... really?!
	public void doAnnounce(Event event, long uploaded, long downloaded, String passkey,  // NOPMD by Eddie on 3/20/10 3:13 AM
			String infoHash, String peerId) throws AnnounceException;
}
