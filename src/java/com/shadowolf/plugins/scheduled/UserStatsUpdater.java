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

import com.shadowolf.plugins.ScheduledPlugin;
import com.shadowolf.tracker.TrackerRequest.Event;
import com.shadowolf.user.User;
import com.shadowolf.user.UserFactory;

public class UserStatsUpdater extends ScheduledPlugin {
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	protected final static Logger LOGGER = Logger.getLogger(UserStatsUpdater.class);
	
	protected boolean running = false;
	protected PreparedStatement stmt;
	protected Set<String> updates = new HashSet<String>();
	protected Connection conn;
	
	public UserStatsUpdater(Attributes attributes) {
		super(attributes);
		
		String table = attributes.getValue("table");
		String downColumn = attributes.getValue("down_column");
		String upColumn = attributes.getValue("up_column");
		String passkeyColumn =  attributes.getValue("passkey_column");
		
		LOGGER.debug("Instatiated UserStatsUpdater plugin.");
		this.updates = Collections.synchronizedSet(this.updates);
		
		try {
			DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
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
	public boolean needsAnnounce() { 
		return true;
	}
	
	public void doAnnounce(Event event, long uploaded, long downloaded, String passkey) {
		if(uploaded > 0 || downloaded > 0) {
			LOGGER.debug("Queuing ... " + passkey + " for update");
			this.addToUpdateQueue(passkey);
		}
	}
	
	public void addToUpdateQueue(String passkey) {
		synchronized(this.updates) {
			this.updates.add(passkey);
		}
	}
	
	@Override
	public void run() {
		if(this.running) {
			return;
		} else {
			this.running = true;
		}
		
		
		boolean failed = false;
		synchronized(this.updates) {
			int total = 0;
			
			if(this.updates.size() > 0) {
				total = (this.updates.size() > 6) ? this.updates.size() / 6 : this.updates.size();
			}
			
			if(total == 0) {
				this.running = false;
				return;
			}
			
			Iterator<String> iter = this.updates.iterator();
		
			for(int i = 0; failed == false && iter.hasNext() && i < total; i++) {
				String passkey = iter.next();
				
				User u = UserFactory.aggregate(passkey);
				this.updates.remove(passkey);
				
				Long down = u.getDownloaded();
				Long up = u.getUploaded();
				
				try {
					this.stmt.setLong(1, down);
					this.stmt.setLong(2, up);
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
