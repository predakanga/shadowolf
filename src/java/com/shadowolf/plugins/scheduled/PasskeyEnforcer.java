package com.shadowolf.plugins.scheduled;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javolution.util.FastSet;

import org.apache.log4j.Logger;

import com.shadowolf.announce.Announce;
import com.shadowolf.plugins.AnnounceFilter;
import com.shadowolf.plugins.ScheduledDBPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.Errors;
import com.shadowolf.util.Exceptions;

public class PasskeyEnforcer extends ScheduledDBPlugin implements AnnounceFilter {
	private final static boolean DEBUG = true;
	protected final static Logger LOGGER = Logger.getLogger(PasskeyEnforcer.class);

	private final String column;
	private final String table;

	protected FastSet<String> hashes = new FastSet<String>();

	public PasskeyEnforcer(final Map<String, String> attributes) {
		this.table = attributes.get("table");
		this.column = attributes.get("passkey_column");

		this.hashes = new FastSet<String>();
		this.hashes.shared();
		
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
			
			final ResultSet results = stmt.getResultSet();
			final FastSet<String> newHashes = new FastSet<String>(this.hashes.size());
			
			try {
				while(results.next()) {
					newHashes.add(results.getString(this.column));
				}
				
				this.hashes = newHashes;
				this.hashes.shared();
				
				this.commit();
			} finally {
				results.close();
				stmt.close();
			}
		} catch (final Exception e) {
			LOGGER.error(Exceptions.logInfo(e));
			this.rollback();
		}

		if(DEBUG) {
			LOGGER.debug("Read " + this.hashes.size());
		}
	}

	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		if(!this.hashes.contains(announce.getPasskey())) {
			throw new AnnounceException(Errors.INVALID_PASSKEY.toString());
		}
	}
}