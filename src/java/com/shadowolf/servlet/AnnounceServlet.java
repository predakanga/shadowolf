package com.shadowolf.servlet;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;

import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerResponse;
import com.shadowolf.tracker.TrackerRequest.Event;
import com.shadowolf.user.Peer;
import com.shadowolf.user.PeerList;
import com.shadowolf.user.User;
import com.shadowolf.user.UserFactory;

public class AnnounceServlet extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(AnnounceServlet.class);
	private static final long serialVersionUID = 1L;
	
	private static Pattern[] whitelist = new Pattern[0];
	private static ConcurrentSkipListMap<String, Boolean> cachedPeerIDs = new ConcurrentSkipListMap<String, Boolean>();
	
	public AnnounceServlet() {
		super();
		PropertyConfigurator.configure(Loader.getResource("log4j.properties"));
	}

	public void init() throws ServletException {
		loadWhitelist();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		/*
		 * Required fields.
		 */
		ServletOutputStream sos = response.getOutputStream();
		LOGGER.debug("Received announce with qs: " + request.getQueryString());
		
		if(request.getParameter("info_hash") == null) {
			sos.print(TrackerResponse.bencoded("Missing parameter: info_hash"));
			return;
		} else if (request.getParameter("peer_id") == null) {
			sos.print(TrackerResponse.bencoded("Missing parameter: peer_id"));
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
			LOGGER.debug("Parameter for event: %%%" + paramEvent + "%%%");
			
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
		
		final String peerId = request.getParameter("peer_id");
		if(checkWhitelist(peerId) == false) {
			sos.print(TrackerResponse.bencoded("Your client is banned."));
			return;
		}

		//sos.print(TrackerResponse.bencoded(request.getParameterMap().keySet().toString())); 
		final byte[] infoHash = request.getParameter("info_hash").getBytes("US-ASCII");
		
		final String port = request.getParameter("port");
		final long uploaded = (request.getParameter("uploaded") != null) ? Long.parseLong(request.getParameter("uploaded")) : 0;
		final long downloaded = (request.getParameter("downloaded") != null) ? Long.parseLong(request.getParameter("downloaded")) : 0;
		
		final long left = (request.getParameter("left") != null) ? Long.parseLong(request.getParameter("left")) : 0;
		LOGGER.debug("Left: " + left);
		
		int numwant = 0;
		if(request.getParameter("numwant") != null) {
			numwant = Integer.parseInt(request.getParameter("numwant")) < 30 ? Integer.parseInt(request.getParameter("numwant")) : 0; 
		} 		
		
		//we're ignoring compact because we don't _need_ to implement the (now useless)
		//full response... and no_peer_id too
		//final boolean compact = request.getParameter("compact") != null;
		//final boolean noPeerId = request.getParameter("no_peer_id") != null;
		final String passkey = request.getParameter("passkey");
		/*
		 * Sanity checks.
		 */
		
		
		try {
			//update user and peer lists
			User u = UserFactory.getUser(peerId, passkey);
			u.updateStats(infoHash, uploaded, downloaded, request.getRemoteAddr(), port);
			
			LOGGER.debug("Event: " + event.toString() + ". Parameters: " + request.getParameterMap().keySet() + "\n");
			Peer p = u.getPeer(infoHash, request.getRemoteAddr(), port);
			PeerList peerlist = PeerList.getList(infoHash);
			
			if(event != Event.STOPPED) {
				if(left > 0) {
					LOGGER.debug("Adding leecher");
					peerlist.addLeecher(p);
				} else {
					LOGGER.debug("Adding seeder");
					peerlist.addSeeder(p);
				}
			} else {
				if(left > 0) { 	
					LOGGER.debug("Removing leecher");
					peerlist.removeLeecher(p); 
					return;
				} else {
					LOGGER.debug("Removing leecher");
					peerlist.removeSeeder(p);
					return;
				}
			}
			
			//prepare return
			int seeders = peerlist.getSeeders().length;
			LOGGER.debug("Total seeders: " + seeders);
			
			int leechers =  peerlist.getLeechers().length;
			LOGGER.debug("Total leechers: " + leechers);
			
			Peer[] peers = null;
			
			if(left > 0){
				peers = peerlist.getSeeders(numwant);
				//LOGGER.debug("Got " + peers.length + " seeders");
			}
			
			if(left > 0 && peers.length < 30) {
				numwant = 30 - peers.length; //29
				Peer[] tempL = peerlist.getLeechers(30 - peers.length);
				LOGGER.debug("GOT " + tempL.length + " leechers");
				peers = (Peer[]) ArrayUtils.addAll(peers, tempL);
			} else {
				peers = peerlist.getLeechers(numwant);
				LOGGER.debug("GOT " + peers.length + " leechers");
			}
			
			
			sos.write(TrackerResponse.bencoded(seeders, leechers, peers, 1, 1));
			//LOGGER.debug(peerlist.getSeeders()[0].getIpAddress());
		} catch (UnknownHostException e) {
			sos.print(TrackerResponse.bencoded("Something is wrong with your reported host."));
			return;
		} catch (IllegalAccessException e) {
			sos.print(TrackerResponse.bencoded("Something went wrong, please contact your site administrator."));
			return;
		} catch (AnnounceException e) {
			sos.print(TrackerResponse.bencoded(e.getMessage()));
			return; 
		} catch (Exception e) {
			e.printStackTrace();
			sos.print(TrackerResponse.bencoded("Something went catastrophically wrong, please contact your site administrator."));
			e.printStackTrace();
		} finally {
			LOGGER.debug("\n\n");
			sos.flush();
		}
	}
    
    public final static boolean checkWhitelist(final String peer_id) {
    	if(cachedPeerIDs.containsKey(peer_id)) {
    		return cachedPeerIDs.get(peer_id).booleanValue();
    	} else {
	    	for(Pattern client : whitelist) {
	    		if(client.matcher(peer_id).find()) {
	    			LOGGER.info("Added whitelist entry for peer_id:    " + peer_id + "         ");
	    			cachedPeerIDs.put(peer_id, new Boolean(true));
	    			return true;
	    		}
	    	}
	    	LOGGER.info("Disallowed client announced:      " + peer_id + "     ");
	    	cachedPeerIDs.put(peer_id, new Boolean(false));
	    	return false;
    	}
    }
    
    private final static void loadWhitelist() {
    	//TODO: un hardcode this and log!
    	whitelist = new Pattern[]  {
    		Pattern.compile("UT.*"),
    		Pattern.compile(".*")
    	};
    }
	
}
