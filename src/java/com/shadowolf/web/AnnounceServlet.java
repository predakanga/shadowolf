package com.shadowolf.web;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.shadowolf.core.application.announce.Announce;
import com.shadowolf.core.application.announce.AnnounceException;
import com.shadowolf.core.application.cache.InfoHashCache;
import com.shadowolf.core.application.plugin.PluginEngine;
import com.shadowolf.core.application.tracker.Client;
import com.shadowolf.core.application.tracker.ClientIdentifier;
import com.shadowolf.core.application.tracker.Registry;
import com.shadowolf.core.application.tracker.response.TrackerResponse;
import com.shadowolf.core.config.Config;
import com.shadowolf.util.Exceptions;

public class AnnounceServlet extends HttpServlet {
	private static final long serialVersionUID = -8466096204269256416L;
	private static final boolean DEBUG = true;
	private static final Logger LOGGER = Logger.getLogger(AnnounceServlet.class);
	private TrackerResponse trackerResponse;
	private Config config;
	private InfoHashCache infoHashCache;
	private PluginEngine engine;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		ServletContext context = servletConfig.getServletContext();
		
		Object config = context.getAttribute("config");
		Object infoHashCache = context.getAttribute("infoHashCache");
		Object engine = context.getAttribute("pluginEngine");
		this.config = (Config)config;
		this.infoHashCache = (InfoHashCache)infoHashCache;
		
		int interval = Integer.parseInt(this.config.getParameter("tracker.interval"));
		int minInterval = Integer.parseInt(this.config.getParameter("tracker.min_interval"));
		this.trackerResponse = new TrackerResponse(interval, minInterval);
		this.engine = (PluginEngine) engine; 
	}


	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		if(DEBUG) {
			LOGGER.debug("Working on: " + request.getQueryString());
		}
		
		final ServletOutputStream sos = response.getOutputStream();
		
		try {
			final Announce announce = new Announce(request);
			this.engine.filterAnnounce(announce);
			this.engine.mutateAnnounce(announce);
			this.engine.doAnnounce(announce);
			
			//we'll assume this is never null because of the info hash enforcement
			Integer torrentId = this.infoHashCache.lookupTorrentId(announce.getInfoHash());
			
			if(DEBUG) {
				LOGGER.debug("Torrent ID: " + torrentId);
			}
			
			Client client = Registry.getClient(announce.getClientIdentifier());
			client.updateStats(torrentId, announce.getUploaded(), announce.getDownloaded());
			
			switch(announce.getEvent()) {
				case ANNOUNCE:
					//do nothing
					break;
				case COMPLETED:
					client.markAsSeeder(torrentId);
					break;
				case STARTED:
					if(announce.getLeft() == 0) {
						client.markAsSeeder(torrentId);
					}
					break;
				case STOPPED: 
					client.removePeer(torrentId);
					break;
			}
			
			Set<ClientIdentifier> set = Registry.getClientList(torrentId);
			
			sos.write(this.trackerResponse.bencodedAnnounce(0, set.size(), set.toArray(new ClientIdentifier[set.size()])));
		} catch (final AnnounceException e) {
			sos.print(e.getMessage());
		} catch (final Exception e) {
			//sos.print(TrackerResponse.bencoded("Something went catastrophically wrong, please contact your site administrator." + e.getClass()));
			LOGGER.error(Exceptions.logInfo(e));
			return;
		} finally {
			sos.flush();
		}
		
		
	}
}
