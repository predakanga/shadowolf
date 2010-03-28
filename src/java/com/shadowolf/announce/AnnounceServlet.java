package com.shadowolf.announce;

import java.io.IOException;

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

/**
 * Announce servlet class.  Processes announces.  You should never need to edit this class, nor call its methods.
 */
public class AnnounceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_PLUGIN_PATH = "/WEB-INF/plugins.xml"; //NOPMD
	private static final Logger LOGGER = Logger.getLogger(AnnounceServlet.class);
	private PluginEngine engine;
	
	/**
	 * Default constructor.  Doesn't do anything but configure logging properties.
	 */
	public AnnounceServlet() {
		super();
		PropertyConfigurator.configure(Loader.getResource("log4j.properties"));
	}

	/**
	 * The initializor.  Creates the plugin engine.
	 * 
	 * @see <a href="http://java.sun.com/products/servlet/2.5/docs/servlet-2_5-mr2/javax/servlet/GenericServlet.html">javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)</a>
	 * @see com.shadowolf.plugins.PluginEngine
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException {
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
		}
		
	}

	/**
	 * Servlet destructor.  Shuts down the plugin engine.
	 * 
	 * @see <a href="http://java.sun.com/products/servlet/2.5/docs/servlet-2_5-mr2/javax/servlet/GenericServlet.html#destroy()">javax.servlet.GenericServlet#destroy()</a>
	 * @see com.shadowolf.plugins.PluginEngine
	 */
	@Override
	public void destroy() {
		super.destroy();
		this.engine.destroy();
	}
	
	/**
	 * Performs the GET request and parses the announce.
	 * @see <a href="http://java.sun.com/products/servlet/2.5/docs/servlet-2_5-mr2/javax/servlet/http/HttpServlet.html#doGet%28javax.servlet.http.HttpServletRequest,%20javax.servlet.http.HttpServletResponse%29">HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)</a>
	 */
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final ServletOutputStream sos = response.getOutputStream();
		
		try {
			final Announce announce = new Announce(request);
			
			this.engine.doAnnounce(announce);
			
			final User user = UserFactory.getUser(announce.getPeerId(), announce.getPasskey());
			
			user.updateStats(announce.getInfoHash(), announce.getUploaded(), 
						announce.getDownloaded(), announce.getIP(), announce.getPort());
			
			final Peer peer = user.getPeer(announce.getInfoHash(), announce.getIP(), announce.getPort());
			final PeerList peerlist = PeerListFactory.getList(announce.getInfoHash());
			
			if(announce.getEvent() != Event.STOPPED && announce.getLeft() > 0) {
				peerlist.addLeecher(peer);
			} else if (announce.getEvent() != Event.STOPPED) {
				peerlist.addSeeder(peer);
			} else if (announce.getLeft() > 0) {
				peerlist.removeLeecher(peer); 
			} else {
				peerlist.removeSeeder(peer);
			}
			
			if(announce.getEvent() == Event.STOPPED) {
				sos.write(TrackerResponse.bencoded(peerlist.getSeederCount(), peerlist.getLeecherCount(), new Peer[0]));
			} else {
				sos.write(TrackerResponse.bencoded(peerlist.getSeederCount(), peerlist.getLeecherCount(), peerlist.getPeers(announce.getNumwant()), 1, 1));
			}
		} catch (AnnounceException e) {
			sos.print(e.getMessage()); 
		} catch (Exception e) {
			sos.print(TrackerResponse.bencoded("Something went catastrophically wrong, please contact your site administrator." + e.getClass()));
			return;
		} finally {
			sos.flush();
		}
	}
}