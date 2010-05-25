package com.shadowolf.core.application.plugin;

import com.shadowolf.core.application.announce.Announce;
import com.shadowolf.core.application.announce.AnnounceException;

public interface AnnounceFilter {
	public void filterAnnounce(Announce announce) throws AnnounceException;
}
