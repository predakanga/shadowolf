package com.shadowolf.servlet;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;

import com.shadowolf.plugins.PluginEngine;
import com.shadowolf.plugins.PluginException;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerResponse;
import com.shadowolf.tracker.TrackerRequest.Event;
import com.shadowolf.user.Peer;
import com.shadowolf.user.PeerList;
import com.shadowolf.user.PeerListFactory;
import com.shadowolf.user.User;
import com.shadowolf.user.UserFactory;
import com.shadowolf.util.Data;

@SuppressWarnings("serial")
public class AnnounceServlet extends HttpServlet {
	public static final String DEFAULT_PLUGIN_PATH = "/WEB-INF/plugins.xml";
	private static final Logger LOGGER = Logger.getLogger(AnnounceServlet.class);
	private static final int DEFAULT_NUMWANT = 200;
	private PluginEngine engine;
	public AnnounceServlet() {
		super();
		PropertyConfigurator.configure(Loader.getResource("log4j.properties"));
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		String path = config.getServletContext().getRealPath(DEFAULT_PLUGIN_PATH);
		
		if(System.getenv("com.shadowolf.plugins.path") != null) {
			path = System.getenv("com.shadowolf.plugins.path");
		}
		
		try {
			this.engine = new PluginEngine(path);
			this.engine.execute();
		} catch (PluginException e) {
			LOGGER.error("Unexpected plugin error... " + e.getCause().getMessage());
			LOGGER.error("Exiting due to plugin exceptions.");
			System.exit(1);
		}
		
	}

	@Override
	public void destroy() {
		super.destroy();
		this.engine.destroy();
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		/*
		 * Required fields.
		 */
		ServletOutputStream sos = response.getOutputStream();
		
		if(request.getParameter("info_hash") == null) {
			sos.print(TrackerResponse.Errors.MISSING_INFO_HASH.toString());
			return;
		} else if (request.getParameter("peer_id") == null) {
			sos.print(TrackerResponse.Errors.MISSING_PEER_ID.toString());
			return;
		} else if (request.getParameter("passkey") == null) {
			sos.print(TrackerResponse.Errors.MISSING_PASSKEY.toString());
			return;
		} else if (request.getParameter("port") == null) {
			sos.print(TrackerResponse.Errors.MISSING_PORT.toString());
			return;
		}
		
		/*
		 * Parse fields.
		 */
		Event event = null; 
		String paramEvent = request.getParameter("event");
		
		if (paramEvent == null)  {
			event = Event.ANNOUNCE;
		} else {
			if(paramEvent.equals("")) {
				event = Event.ANNOUNCE;
			} else if(paramEvent.equals("completed")) {
				event = Event.COMPLETED;
			} else if(paramEvent.equals("stopped")) {
				event = Event.STOPPED;
			} else if(paramEvent.equals("started")) {
				event = Event.STARTED;
			}
		}

		//pre-validated fields
		final String peerId = request.getParameter("peer_id");
		
		final String encoding = request.getCharacterEncoding() == null ? "ISO-8859-1" : request.getCharacterEncoding();
		final String infoHash = Data.byteArrayToHexString(request.getParameter("info_hash").getBytes(encoding)); 
		final String port = request.getParameter("port");
		final String passkey = request.getParameter("passkey");
		
		//numeric fields _should_ be there, but I'm offering no guarantee
		final long uploaded = (request.getParameter("uploaded") != null) ? Long.parseLong(request.getParameter("uploaded")) : 0;
		final long downloaded = (request.getParameter("downloaded") != null) ? Long.parseLong(request.getParameter("downloaded")) : 0;		
		final long left = (request.getParameter("left") != null) ? Long.parseLong(request.getParameter("left")) : 0;
		
		int numwant = 0;
		if(request.getParameter("numwant") != null) {
			numwant = Integer.parseInt(request.getParameter("numwant")) < DEFAULT_NUMWANT ? Integer.parseInt(request.getParameter("numwant")) : DEFAULT_NUMWANT; 
		} 		
		
		//we're ignoring compact because we don't _need_ to implement the (now useless)
		//full response... and no_peer_id too
		
		try {
			this.engine.doAnnounce(event, uploaded, downloaded, passkey, infoHash, peerId);
			
			User u = UserFactory.getUser(peerId, passkey);
			u.updateStats(infoHash, uploaded, downloaded, request.getRemoteAddr(), port);
			
			Peer p = u.getPeer(infoHash, request.getRemoteAddr(), port);
			PeerList peerlist = PeerListFactory.getList(infoHash);
			
			if(event != Event.STOPPED) {
				if(left > 0) {
					peerlist.addLeecher(p);
				} else {
					peerlist.addSeeder(p);
				}
			} else {
				if(left > 0) { 	
					peerlist.removeLeecher(p); 
				} else {
					peerlist.removeSeeder(p);
				}
			}
			
			//prepare return
			int seeders = peerlist.getSeederCount();
			int leechers =  peerlist.getLeecherCount();
			
			if(event == Event.STOPPED){
				sos.write(TrackerResponse.bencoded(seeders, leechers, new Peer[0]));
				return;
			}
			
			Peer[] peers = peerlist.getPeers(numwant);
			
			sos.write(TrackerResponse.bencoded(seeders, leechers, peers, 1, 1));
		} catch (UnknownHostException e) {
			sos.print(TrackerResponse.bencoded("Something is wrong with your reported host."));
			return;
		} catch (IllegalAccessException e) {
			sos.print(TrackerResponse.bencoded("Something went wrong, please contact your site administrator."));
			return;
		} catch (AnnounceException e) {
			sos.print(e.getMessage());
			return; 
		} catch (Exception e) {
			LOGGER.error(e.getClass());
			LOGGER.error(e.getCause());
			LOGGER.error(e.getMessage());
			e.printStackTrace();
			sos.print(TrackerResponse.bencoded("Something went catastrophically wrong, please contact your site administrator." + e.getClass()));
			return;
		} finally {
			sos.flush();
		}
	}
}
