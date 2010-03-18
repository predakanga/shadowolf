package com.shadowolf.plugins.scheduled;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.shadowolf.plugins.ScheduledPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerRequest.Event;
import com.shadowolf.tracker.TrackerResponse.Errors;

public class Whitelist extends ScheduledPlugin {
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	protected final static Logger LOGGER = Logger.getLogger(Whitelist.class);
	private final String column;
	
	private String[] peerIds = new String[0];
	
	private PreparedStatement stmt;
	private Connection conn;
	
	public Whitelist(Attributes attributes) {
		super(attributes);
		
		String table = attributes.getValue("table");
		this.column = attributes.getValue("peer_id_column");
		
		
		try {
			DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
			this.conn = source.getConnection();
			conn.setAutoCommit(true);
			
			this.stmt = conn.prepareStatement("SELECT " + column + " FROM " + table);
		} catch (NamingException n) {
			LOGGER.error("Unexpected NamingException...");
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
		
		
	}
	
	@Override
	public int getInitialDelay() {
		return 0;
	}
	
	@Override
	public boolean needsAnnounce() {
		return true;
	}
	
	@Override
	public void run() {
		try {
			this.stmt.execute();
			ResultSet rs = this.stmt.getResultSet();
			ArrayList<String> tempIds = new ArrayList<String>();
			
			LOGGER.debug(rs.getStatement());
			while(rs.next()) {
				tempIds.add(rs.getString(this.column));
			}
				
			synchronized(this.peerIds) {
				this.peerIds = tempIds.toArray(new String[tempIds.size()]);	
			}
			
			rs.close();
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
		
		LOGGER.debug("Read " + this.peerIds.length);
	}
	
	@Override
	public void doAnnounce(Event e, long uploaded, long downloaded, String passkey, String infoHash, String peerId) throws AnnounceException{
		for(String s : this.peerIds) {
			if(peerId.startsWith(s)) {
				return;
			}
		}
		
		throw new AnnounceException(Errors.BANNED_CLIENT.toString());
	}

}
