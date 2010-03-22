package com.shadowolf.plugins.scheduled;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
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
import com.shadowolf.tracker.TrackerResponse.Errors;

public class Whitelist extends ScheduledPlugin implements AnnounceFilter {
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	protected final static Logger LOGGER = Logger.getLogger(Whitelist.class);
	private transient final String column;
	
	private transient Set<String> peerIds = Collections.synchronizedSet(new HashSet<String>());
	
	private transient PreparedStatement stmt;
	
	public Whitelist(final Attributes attributes) {
		super(attributes);
		
		final String table = attributes.getValue("table");
		this.column = attributes.getValue("peer_id_column");
		
		
		try {
			final DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
			final Connection conn = source.getConnection();
			conn.setAutoCommit(true);
			
			this.stmt = conn.prepareStatement("SELECT " + column + " FROM " + table);
		} catch (NamingException n) {
			LOGGER.error("Unexpected NamingException...");
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
		
		
	}
	
	@Override
	public void run() {
		try {
			this.stmt.execute();
			final ResultSet results = this.stmt.getResultSet();
			try {
				final Set<String> tempIds = Collections.synchronizedSet(new HashSet<String>());
				
				while(results.next()) {
					tempIds.add(results.getString(this.column));
				}
					
				synchronized(this.peerIds) {
					this.peerIds = tempIds;
				}
			} finally {
				results.close();
			}
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
	}
	
	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		
		for(String s : this.peerIds) {
			if(announce.getPeerId().startsWith(s)) {
				return;
			}
		}
		
		throw new AnnounceException(Errors.BANNED_CLIENT.toString());
	}

}
