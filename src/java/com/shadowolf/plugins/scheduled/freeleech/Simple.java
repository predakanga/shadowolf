package com.shadowolf.plugins.scheduled.freeleech;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javolution.util.FastSet;

import org.apache.log4j.Logger;

import com.shadowolf.announce.Announce;
import com.shadowolf.plugins.AnnounceAction;
import com.shadowolf.plugins.ScheduledDBPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.util.Data;

public class Simple extends ScheduledDBPlugin implements AnnounceAction {
	private final boolean DEBUG = true;
	protected final static Logger LOGGER = Logger.getLogger(Simple.class);
	private FastSet<String> freeHashes;

	private final String column;
	private final String table;
	private final String flag;


	public Simple(final Map<String, String> attributes) {
		this.table = attributes.get("table");
		this.flag = attributes.get("torrent_freeleech_flag");
		this.column = attributes.get("info_hash_column");
		
		this.freeHashes = new FastSet<String>();
		this.freeHashes.shared();
		this.run();
	}

	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		if(this.freeHashes.contains(announce.getInfoHash())) {
			announce.setDownloaded(0);
		}
	}

	@Override
	public void run() {
		if(this.DEBUG) {
			LOGGER.debug("Reading freeleech out of the database...");
		}

		try {
			final PreparedStatement stmt = this.prepareStatement("SELECT " + this.column + " FROM " + this.table + " WHERE " + this.flag + "='1'");

			if(stmt == null) {
				LOGGER.error("COULD NOT PREPARE STATEMENT");
				return;
			}

			stmt.execute();

			final ResultSet results = stmt.getResultSet();

			try {
				final FastSet<String> tempIds = new FastSet<String>();
				tempIds.shared();
				
				while(results.next()) {
					final Blob infoBlob = results.getBlob(this.column);

					try {
						final byte[] blobString = infoBlob.getBytes(1l, (int) infoBlob.length());

						tempIds.add(Data.byteArrayToHexString(blobString));
					} finally {
						//infoBlob.free();
					}
				}

				if(this.DEBUG) {
					LOGGER.debug("Found " + tempIds.size() + " free torrents");
				}

				this.freeHashes = tempIds;
			} finally {
				results.close();
				stmt.close();
			}

			this.commit();
		} catch (final SQLException e) {
			this.rollback();
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}

	}

}
