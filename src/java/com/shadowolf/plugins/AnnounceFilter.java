package com.shadowolf.plugins;

import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerRequest.Event;

public interface AnnounceFilter {
	public void doAnnounce(Event e, long uploaded, long downloaded, String passkey, 
			String infoHash, String peerId) throws AnnounceException;
}
