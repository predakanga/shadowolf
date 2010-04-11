package com.shadowolf.plugins.scheduled;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.log4j.Logger;

import com.shadowolf.announce.Announce;
import com.shadowolf.plugins.AnnounceFilter;
import com.shadowolf.plugins.ScheduledDBPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.Errors;
import com.shadowolf.util.Data;

public class InfoHashEnforcer extends ScheduledDBPlugin implements AnnounceFilter {
	private final static boolean DEBUG = false;
	protected final static Logger LOGGER = Logger.getLogger(InfoHashEnforcer.class);
	private final String column;
	private final String table;

	protected ConcurrentSkipListSet<String> hashes = new ConcurrentSkipListSet<String>(); //NOPMD ... not a bean.

	public InfoHashEnforcer(final Map<String, String> attributes) {

		this.table = attributes.get("table");
		this.column = attributes.get("info_hash_column");

		this.run();
	}

	@Override
	public void run() {
		try {
			final PreparedStatement stmt = this.prepareStatement("SELECT " + this.column + " FROM " + this.table);

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

					this.hashes.add(Data.byteArrayToHexString(blobString));
				}

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
	public void doAnnounce(final Announce announce) throws AnnounceException {
		if(DEBUG) {
			LOGGER.debug("Checking " + announce.getInfoHash() + " against cache of " + this.hashes.size());
		}

		if(!this.hashes.contains(announce.getInfoHash())) {
			throw new AnnounceException(Errors.TORRENT_NOT_REGISTERED.toString());
		}
	}
}

