package com.shadowolf.plugins.scheduled.freeleech;

import java.sql.Blob;
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
import com.shadowolf.util.Data;

public class Simple extends ScheduledPlugin implements AnnounceFilter {
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	private final boolean DEBUG = true;
	protected final static Logger LOGGER = Logger.getLogger(Simple.class);
	private Set<String> freeHashes = Collections.synchronizedSet(new HashSet<String>());
	
	private String column;
	private PreparedStatement stmt;
	
	public Simple(Attributes attributes) {
		super(attributes);
		
		final String table = attributes.getValue("table");
		final String flag = attributes.getValue("torrent_freeleech_flag");
		this.column = attributes.getValue("info_hash_column");
		
		
		try {
			final DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
			final Connection conn = source.getConnection();
			conn.setAutoCommit(true);
			
			this.stmt = conn.prepareStatement("SELECT " + column + " FROM " + table + " WHERE " + flag + "='1'");
			
			if(DEBUG) {
				LOGGER.debug(this.stmt);
			}
		} catch (NamingException n) {
			LOGGER.error("Unexpected NamingException...");
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
		
			
	}
	
	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		if(this.freeHashes.contains(announce.getInfoHash())) {
			LOGGER.debug("Free announce!");
			announce.setDownloaded(0);
		}
	}
	
	@Override
	public void run() {
		if(DEBUG) {
			LOGGER.debug("Reading freeleech out of the database...");
		}
		
		try {
			this.stmt.execute();
			final ResultSet results = this.stmt.getResultSet();
			try {
				final Set<String> tempIds = Collections.synchronizedSet(new HashSet<String>());
				
				while(results.next()) {
					final Blob infoBlob = results.getBlob(this.column);

					try {
						final byte[] blobString = infoBlob.getBytes(1l, (int) infoBlob.length());
				
						tempIds.add(Data.byteArrayToHexString(blobString));
					} finally {
						//infoBlob.free();
					}
				}
				
				if(DEBUG) {
					LOGGER.debug("Found " + tempIds.size() + " free torrents");
				}
				
				synchronized(this.freeHashes) {
					this.freeHashes = tempIds;
				}
			} finally {
				results.close();
			}
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}

	}

}
