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

import com.shadowolf.announce.Announce;
import com.shadowolf.plugins.AnnounceFilter;
import com.shadowolf.plugins.ScheduledPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerResponse;
import com.shadowolf.util.Data;

public class InfoHashEnforcer extends ScheduledPlugin implements AnnounceFilter {
	private final static boolean DEBUG = true;
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
			
			if(DEBUG) {
				LOGGER.debug(this.stmt);
			}
		} catch (NamingException n) {
			LOGGER.error("Unexpected NamingException...");
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
		
		this.run();
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

					final byte[] blobString = infoBlob.getBytes(1l, (int) infoBlob.length());
			
					hashes.add(Data.byteArrayToHexString(blobString));
				}
			} finally {
				resultSet.close();
			}
			
			if(DEBUG) {
				LOGGER.debug("Read " + this.hashes.size() + " info_hashes");
			}
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		LOGGER.debug("Read " + this.hashes.size());
	}
	
	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		if(DEBUG) {
			LOGGER.debug("Checking " + announce.getInfoHash() + " against cache of " + this.hashes.size());
		}
		
		if(!this.hashes.contains(announce.getInfoHash())) {
			throw new AnnounceException(TrackerResponse.Errors.TORRENT_NOT_REGISTERED.toString());
		}
	}
}

