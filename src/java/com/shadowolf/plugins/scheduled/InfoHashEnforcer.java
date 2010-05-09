package com.shadowolf.plugins.scheduled;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javolution.util.FastSet;

import org.apache.log4j.Logger;

import com.shadowolf.announce.Announce;
import com.shadowolf.plugins.AnnounceFilter;
import com.shadowolf.plugins.ScheduledDBPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.Errors;
import com.shadowolf.util.Data;

public class InfoHashEnforcer extends ScheduledDBPlugin implements AnnounceFilter {
	private final static boolean DEBUG = false;
	private final static Logger LOGGER = Logger.getLogger(InfoHashEnforcer.class);
	private final String column;
	private final String table;
	    
	protected FastSet<String> hashes; 
	
	public InfoHashEnforcer(final Map<String, String> attributes) {
		this.hashes = new FastSet<String>();
		this.hashes.shared();
		
		this.table = attributes.get("table");
		this.column = attributes.get("info_hash_column");

		this.run();
	}

	public void addHash(final String newHash) {
		this.hashes.add(newHash);
	}
	
	public boolean removeHash(final String hash) {
		if(this.hashes.contains(hash)) {
			this.hashes.remove(hash);
			return true; 
		} else {
			return false;
		}
	}
	
	@Override
	public void run() {
		try {
			final PreparedStatement stmt = this.prepareStatement("SELECT " + this.column + " FROM " + this.table);
			final FastSet<String> newHashes = new FastSet<String>(this.hashes.size());
			if(stmt == null) {
				LOGGER.error("Could not prepare statement!");
				return;
			}

			stmt.execute();
			final ResultSet resultSet =  stmt.getResultSet();

			try {
				while(resultSet.next()) {
					final Blob infoBlob = resultSet.getBlob(this.column);

					final byte[] blobString = infoBlob.getBytes(1l, (int) infoBlob.length());

					newHashes.add(Data.byteArrayToHexString(blobString));
				}
				
				this.hashes = newHashes;
				this.hashes.shared();
				
				this.commit();
			} finally {
				stmt.close();
				resultSet.close();
			}

			if(DEBUG) {
				LOGGER.debug("Read " + this.hashes.size() + " info_hashes");
			}
		} catch (final SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage());// + "\t Cause: " + e.getCause().getMessage());
			this.rollback();
		}

		if(DEBUG) {
			LOGGER.debug("Read " + this.hashes.size());
		}
	}

	@Override
	public void filterAnnounce(final Announce announce) throws AnnounceException {
		if(DEBUG) {
			LOGGER.debug("Checking " + announce.getInfoHash() + " against cache of " + this.hashes.size());
		}

		if(!this.hashes.contains(announce.getInfoHash())) {
			throw new AnnounceException(Errors.TORRENT_NOT_REGISTERED);
		}
	}
}

