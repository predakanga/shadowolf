package com.shadowolf.scrape;

import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;

import com.shadowolf.config.Config;
import com.shadowolf.plugins.ScheduledDBPlugin;
import com.shadowolf.tracker.ScrapeException;
import com.shadowolf.tracker.TrackerResponse;

/**
 * Scrape servlet class. Does scrapy things
 */
public class ScrapeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logger.getLogger(ScrapeServlet.class);
	private static final boolean DEBUG = true;
	
	/**
	 * Here, we keep the completed totals.
	 */
	public static ConcurrentHashMap<String, Integer> completedTotals = new ConcurrentHashMap<String, Integer>();

	/**
	 * Of course, in order to fetch them, we need some sort of thing that's hooked into the db.
	 * We'll execute the pseudo-plugin through this executor.
	 */
	private transient final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
	
	/**
	 * Default constructor.  Doesn't do anything but configure logging properties.
	 */
	public ScrapeServlet() {
		super();
		PropertyConfigurator.configure(Loader.getResource("log4j.properties"));
	}
	
	/**
	 * Set up the config, and also instantiate a TotalsUpdater and schedule it.
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		
		if (!Config.isInitialized()) {
			Config.init(config.getServletContext());
		}
		
		final TotalsUpdater totalsUpdater = new TotalsUpdater();
		totalsUpdater.run();
		
		this.executor.scheduleAtFixedRate(totalsUpdater, 15, 15, TimeUnit.MINUTES);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		this.executor.shutdownNow();
		Config.destroy();
	}
	
	/**
	 * Performs the GET request and parses the announce.
	 * @see <a href="http://java.sun.com/products/servlet/2.5/docs/servlet-2_5-mr2/javax/servlet/http/HttpServlet.html#doGet%28javax.servlet.http.HttpServletRequest,%20javax.servlet.http.HttpServletResponse%29">HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)</a>
	 */
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final ServletOutputStream sos = response.getOutputStream();
		
		try {
			final String scrape = ScrapeResponseFactory.scrape(request.getCharacterEncoding(), request.getParameterValues("info_hash"));
			sos.print(scrape);
			
		
		} catch (ScrapeException e) {
			sos.print(e.getMessage()); 
		} catch (Exception e) {
			LOGGER.error("Unexpected exception was thrown", e);
			sos.print(TrackerResponse.bencoded("Something went horribly wrong."));
		} finally {
			sos.flush();
		}
	}
	
	//PMD says that we can't have threads. boohoo.
	private class TotalsUpdater extends ScheduledDBPlugin implements Runnable { //NOPMD - fuck you, I'm an anteater
		private transient final String torrentTable;
		private transient final String torrentIDColumn;
		private transient final String infoHashColumn;
		private transient final String snatchedTable;
		private transient final String snatchedTorrentID;
		private transient final String snatchedUserID;
		
		public TotalsUpdater() {
			super();
			this.torrentTable = Config.getParameter("scrape.torrent_table");
			this.torrentIDColumn = Config.getParameter("scrape.torrent_id_column");
			this.infoHashColumn = Config.getParameter("scrape.info_hash_column");
			this.snatchedTable = Config.getParameter("scrape.snatched_table");
			this.snatchedTorrentID = Config.getParameter("scrape.snatched_torrent_id_column");
			this.snatchedUserID = Config.getParameter("scrape.snatched_user_id_column");
		}
		
		@Override
		public void run() {
			try {				
				final PreparedStatement stmt = this.prepareStatement(
						"SELECT " + this.torrentTable + "." + this.infoHashColumn + ", COUNT(" + this.snatchedTable  + "." + this.snatchedUserID + ")" +
						" FROM " + this.torrentTable + " INNER JOIN " + this.snatchedTable +
						" ON " + this.snatchedTable + "." + this.snatchedTorrentID + " = " + this.torrentTable + "." + this.torrentIDColumn + 
						" GROUP BY " + this.torrentTable + "." + this.torrentIDColumn
				);
				
				if (stmt == null) {
					LOGGER.error("Failed to prepare the statement for updating the completed totals");
					return;
				}
				
				final ResultSet result = stmt.executeQuery(); //NOPMD we _are_ closing it
				
				if (DEBUG) {
					LOGGER.debug("Fetching " + result.getFetchSize());
				}
				
				while (result.next()) {
					ScrapeServlet.completedTotals.put(result.getString(1), result.getInt(2));
				}
				
				result.close();
				
			} catch (SQLException e) {
				LOGGER.error("Something threw an SQLException", e);
			}
		}
	}
}
