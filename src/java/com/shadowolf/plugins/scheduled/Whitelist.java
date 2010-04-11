package com.shadowolf.plugins.scheduled;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.shadowolf.announce.Announce;
import com.shadowolf.announce.Event;
import com.shadowolf.plugins.AnnounceFilter;
import com.shadowolf.plugins.ScheduledDBPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.Errors;

public class Whitelist extends ScheduledDBPlugin implements AnnounceFilter {
	private final boolean DEBUG = false;
	protected final static Logger LOGGER = Logger.getLogger(Whitelist.class);

	private final String column;
	private final String table;

	private Set<String> peerIds = Collections.synchronizedSet(new HashSet<String>());


	public Whitelist(final Map<String, String> attributes) {
		this.table = attributes.get("table");
		this.column = attributes.get("peer_id_column");

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
				final Set<String> tempIds = Collections.synchronizedSet(new HashSet<String>());

				while(results.next()) {
					tempIds.add(results.getString(this.column));
				}

				synchronized(this.peerIds) {
					this.peerIds = tempIds;
				}

				this.commit();
			} finally {
				results.close();
				stmt.close();
			}
		} catch (final SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage());// + "\t Cause: " + e.getCause().getMessage());
			this.rollback();
		}
	}

	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		if(announce.getEvent() == Event.STOPPED) {
			return;
		}

		for(final String s : this.peerIds) {
			if(this.DEBUG) {
				LOGGER.debug(announce.getPeerId() + "\t" + s);
			}
			if(announce.getPeerId().startsWith(s)) {
				return;
			}
		}

		throw new AnnounceException(Errors.BANNED_CLIENT.toString());
	}

}
