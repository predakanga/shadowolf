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

import com.shadowolf.plugins.AnnounceFilter;
import com.shadowolf.plugins.ScheduledPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerResponse;
import com.shadowolf.tracker.TrackerRequest.Event;

public class PasskeyEnforcer extends ScheduledPlugin implements AnnounceFilter {
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	protected final static Logger LOGGER = Logger.getLogger(PasskeyEnforcer.class);
	private final String column;
	
	protected ConcurrentSkipListSet<String> hashes = new ConcurrentSkipListSet<String>();
	private PreparedStatement stmt;
	private Connection conn;
	
	public PasskeyEnforcer(Attributes attributes) {
		super(attributes);
		
		String table = attributes.getValue("table");
		this.column = attributes.getValue("passkey_column");
		
		
		try {
			DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
			this.conn = source.getConnection();
			conn.setAutoCommit(false);
			
			this.stmt = conn.prepareStatement("SELECT " + column + " FROM " + table);
		} catch (NamingException n) {
			LOGGER.error("Unexpected NamingException...");
			n.printStackTrace();
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
			ResultSet rs = this.stmt.getResultSet();
			while(rs.next()) {
				hashes.add(rs.getString(this.column));
			}
			
			rs.close();
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
		
		LOGGER.debug("Read " + this.hashes.size());
	}
	
	@Override
	public void doAnnounce(Event e, long uploaded, long downloaded, String passkey, String infoHash, String peerId) throws AnnounceException{
		if(this.hashes.contains(passkey) == false) {
			throw new AnnounceException(TrackerResponse.Errors.INVALID_PASSKEY.toString());
		}
	}
}