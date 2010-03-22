package com.shadowolf.plugins.scheduled;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.shadowolf.announce.Announce;
import com.shadowolf.plugins.AnnounceFilter;
import com.shadowolf.plugins.ScheduledPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerResponse;

public class PasskeyEnforcer extends ScheduledPlugin implements AnnounceFilter {
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	protected final static Logger LOGGER = Logger.getLogger(PasskeyEnforcer.class);
	
	private transient final String column;
	protected transient ConcurrentSkipListSet<String> hashes = new ConcurrentSkipListSet<String>();
	private transient PreparedStatement stmt;
	
	public PasskeyEnforcer(final Attributes attributes) {
		super(attributes);
		
		final String table = attributes.getValue("table");
		this.column = attributes.getValue("passkey_column");
		
		
		try {
			final DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
			final Connection conn = source.getConnection();
			conn.setAutoCommit(false);
			
			this.stmt = conn.prepareStatement("SELECT " + column + " FROM " + table);
		} catch (NamingException n) {
			LOGGER.error("Unexpected NamingException...");
			LOGGER.error(n.getCause());
			LOGGER.error(n.getMessage());
			LOGGER.error(n.getExplanation());
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
		
		
	}
	
	@Override
	public int getInitialDelay() {
		return 0;
	}
	
	@Override
	public void run() {
		
		try {
			this.stmt.execute();
			final ResultSet results = this.stmt.getResultSet();
			
			try {
				while(results.next()) {
					hashes.add(results.getString(this.column));
				}
			} finally { 
				results.close();
			}
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
		
		LOGGER.debug("Read " + this.hashes.size());
	}
	
	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		
		if(!this.hashes.contains(announce.getPasskey())) {
			throw new AnnounceException(TrackerResponse.Errors.INVALID_PASSKEY.toString());
		}
	}
}