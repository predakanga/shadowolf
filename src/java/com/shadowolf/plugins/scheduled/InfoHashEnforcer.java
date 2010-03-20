package com.shadowolf.plugins.scheduled;

import java.sql.Blob;
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
import com.shadowolf.util.Data;

public class InfoHashEnforcer extends ScheduledPlugin implements AnnounceFilter {
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	protected final static Logger LOGGER = Logger.getLogger(InfoHashEnforcer.class);
	private final String column; //NOPMD ... not a bean.
	
	protected ConcurrentSkipListSet<String> hashes = new ConcurrentSkipListSet<String>(); //NOPMD ... not a bean.
	private PreparedStatement stmt; //NOPMD ... not a bean.
	private Connection conn; //NOPMD ... not a bean.
	
	public InfoHashEnforcer(final Attributes attributes) {
		super(attributes);
		
		final String table = attributes.getValue("table");
		this.column = attributes.getValue("info_hash_column");
		
		
		try {
			final DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
			this.conn = source.getConnection();
			conn.setAutoCommit(false);
			
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
	public void run() {
		try {
			this.stmt.execute();
			final ResultSet resultSet = this.stmt.getResultSet();
			
			try {
				while(resultSet.next()) {
					final Blob infoBlob = resultSet.getBlob(this.column);

					try {
						final byte[] blobString = infoBlob.getBytes(1l, (int) infoBlob.length());
				
						hashes.add(Data.byteArrayToHexString(blobString));
					} finally {
						infoBlob.free();
					}
				}
			} finally {
				resultSet.close();
			}
			
			
			LOGGER.debug("Read " + this.hashes.size() + " info_hashes");
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
		
		LOGGER.debug("Read " + this.hashes.size());
	}
	
	@Override
	public void doAnnounce(final Event event, final long uploaded, final long downloaded, final String passkey, 
			final String infoHash, final String peerId) throws AnnounceException{
		
		if(!this.hashes.contains(infoHash)) {
			throw new AnnounceException(TrackerResponse.Errors.TORRENT_NOT_REGISTERED.toString());
		}
	}
}

