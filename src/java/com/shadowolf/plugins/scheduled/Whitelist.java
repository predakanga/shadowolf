package com.shadowolf.plugins.scheduled;

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
import com.shadowolf.util.Exceptions;

public class Whitelist extends ScheduledDBPlugin implements AnnounceFilter {
	private final boolean DEBUG = false;
	protected final static Logger LOGGER = Logger.getLogger(Whitelist.class);

	private final String column;
	private final String table;

	private FastSet<String> peerIds;


	public Whitelist(final Map<String, String> attributes) {
		this.table = attributes.get("table");
		this.column = attributes.get("peer_id_column");
		
		this.peerIds = new FastSet<String>();
		this.peerIds.shared();
		
		this.run();
	}

	@Override
	public void run() {
		if(this.DEBUG) {
			LOGGER.debug("Reading whitelist out of the database...");
		}

		try {
			final PreparedStatement stmt = this.prepareStatement("SELECT " + this.column + " FROM " + this.table);

			if(stmt == null) {
				LOGGER.error("Could not prepare statement!");
				return;
			}

			stmt.execute();

			final ResultSet results = stmt.getResultSet();
			try {
				final FastSet<String> tempIds = new FastSet<String>();
				tempIds.shared();

				while(results.next()) {
					tempIds.add(results.getString(this.column));
				}

				this.peerIds = tempIds;

				this.commit();
			} finally {
				results.close();
				stmt.close();
			}
		} catch (final SQLException e) {
			LOGGER.error(Exceptions.logInfo(e));
			this.rollback();
		}
	}

	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		for(final String s : this.peerIds) {
			if(this.DEBUG) {
				LOGGER.debug(announce.getPeerId() + "\t" + s);
			}
			if(announce.getPeerId().startsWith(s)) {
				return;
			}
		}

		throw new AnnounceException(Errors.BANNED_CLIENT);
	}

}
