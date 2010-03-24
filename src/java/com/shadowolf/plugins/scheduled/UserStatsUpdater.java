package com.shadowolf.plugins.scheduled;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.shadowolf.announce.Announce;
import com.shadowolf.plugins.AnnounceFilter;
import com.shadowolf.plugins.ScheduledPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.user.User;
import com.shadowolf.user.UserFactory;

public class UserStatsUpdater extends ScheduledPlugin implements AnnounceFilter {
	private final static boolean DEBUG = false;
	
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	protected final static Logger LOGGER = Logger.getLogger(UserStatsUpdater.class);
	
	protected transient boolean running = false;
	protected transient PreparedStatement stmt;
	protected transient Set<String> updates = new HashSet<String>();
	protected transient Connection conn;
	
	public UserStatsUpdater(final Attributes attributes) {
		super(attributes);
		
		final String table = attributes.getValue("table");
		final String downColumn = attributes.getValue("down_column");
		final String upColumn = attributes.getValue("up_column");
		final String passkeyColumn =  attributes.getValue("passkey_column");
		
		LOGGER.debug("Instatiated UserStatsUpdater plugin.");
		this.updates = Collections.synchronizedSet(this.updates);
		
		try {
			final DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
			this.conn = source.getConnection();
			conn.setAutoCommit(false);
			
			this.stmt = conn.prepareStatement(
				"UPDATE " + table + " SET " + downColumn + "= " + downColumn + "+ ?, " 
				+ upColumn + "= " + upColumn + " + ? WHERE " + passkeyColumn + "=?");
		} catch (NamingException n) {
			LOGGER.error("Unexpected NamingException...");
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
	}
	
	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		if(announce.getUploaded() > 0 || announce.getDownloaded() > 0) {
		//	LOGGER.debug("Queuing ... " + passkey + " for update");
			this.addToUpdateQueue(announce.getPasskey());
		}
	}
	
	public void addToUpdateQueue(final String passkey) {
		synchronized(this.updates) {
			if(DEBUG) { 
				LOGGER.debug("Adding " + passkey + " to update queue.");
			}
			this.updates.add(passkey);
		}
	}
	
	@Override
	public void run() {
		boolean failed = false;
		synchronized(this.updates) {
			LOGGER.debug("Running updates...");
			final int total = this.updates.size();
			
			if(total == 0) {
				this.running = false;
				return;
			}
			
			final Iterator<String> iter = this.updates.iterator();
		
			for(int i = 0; !failed && iter.hasNext() && i < total; i++) {
				final String passkey = iter.next();
				LOGGER.debug("Updating... " + passkey);
				
				final User userAggregate = UserFactory.aggregate(passkey);
				this.updates.remove(passkey);
				
				userAggregate.resetStats(); //this is necessary in cases where stats do NOT get aggregated
				
				LOGGER.debug("Adding down: " + userAggregate.getDownloaded());
				LOGGER.debug("Adding up: " +  userAggregate.getUploaded());

				try {
					this.stmt.setLong(1, userAggregate.getDownloaded());
					this.stmt.setLong(2,  userAggregate.getUploaded());
					this.stmt.setString(3, passkey);
					this.stmt.executeUpdate();
				} catch (SQLException e) {
					failed = true;
				} 
			}
		}
		
		try {
			if(failed) {
				this.conn.rollback();
			} else {
				this.conn.commit();
			}
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}

		this.running = false;
	}
}
