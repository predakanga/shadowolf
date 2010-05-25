package com.shadowolf.plugins.enforcers;

import java.util.Map;

import javax.servlet.ServletContext;

import com.shadowolf.core.application.announce.Announce;
import com.shadowolf.core.application.announce.AnnounceException;
import com.shadowolf.core.application.cache.InfoHashCache;
import com.shadowolf.core.application.plugin.AbstractPlugin;
import com.shadowolf.core.application.plugin.AnnounceFilter;
import com.shadowolf.core.application.tracker.response.Errors;

public class InfoHashEnforcer extends AbstractPlugin implements AnnounceFilter {
	private final InfoHashCache cache;
	
	public InfoHashEnforcer(Map<String, String> options, ServletContext context) {
		this.cache = (InfoHashCache) context.getAttribute("infoHashCache");
	}
	
	@Override
	public void filterAnnounce(Announce announce) throws AnnounceException {
		if(this.cache.lookupTorrentId(announce.getInfoHash()) == null) {
			throw new AnnounceException(Errors.TORRENT_NOT_REGISTERED);
		}
	}

	@Override
	public void init() {
		//do nothing
	}

	@Override
	public void destroy() {
		// do nothing

	}

}
