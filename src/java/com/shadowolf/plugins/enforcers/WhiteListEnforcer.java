package com.shadowolf.plugins.enforcers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javolution.util.FastSet;

import org.apache.log4j.Logger;

import com.shadowolf.core.application.announce.Announce;
import com.shadowolf.core.application.announce.AnnounceException;
import com.shadowolf.core.application.plugin.AbstractScheduledPlugin;
import com.shadowolf.core.application.plugin.AnnounceFilter;
import com.shadowolf.core.application.tracker.response.Errors;
import com.shadowolf.core.database.DatabaseWrapper;
import com.shadowolf.util.Exceptions;

public class WhiteListEnforcer extends AbstractScheduledPlugin implements AnnounceFilter {
	//private final boolean DEBUG = false;
	protected final static Logger LOGGER = Logger.getLogger(WhiteListEnforcer.class);

	private final String statement;

	private Set<String> peerIds = Collections.emptySet();

	private PeerFetcher fetcher = new PeerFetcher();
	
	public WhiteListEnforcer(final Map<String, String> options) {
		super(options);
		final String column = options.get("peer_id_column");
		final String table = options.get("table");

		this.statement = "SELECT " + column + " FROM " + table;
	}

	@Override
	public void run() {
		this.fetcher.run();
	}

	@Override
	public void filterAnnounce(final Announce announce) throws AnnounceException {
		for(final String s : this.peerIds) {
			if(announce.getPeerId().startsWith(s)) {
				return;
			}
		}
		
		throw new AnnounceException(Errors.BANNED_CLIENT);
	}

	@Override
	public void init() {
		this.fetcher.run();
	}

	@Override
	public void destroy() {
		//do nothing
	}

	private class PeerFetcher extends DatabaseWrapper implements Runnable {
		@Override
		public void run() {
			try {
				final PreparedStatement stmt = this.prepareStatement(WhiteListEnforcer.this.statement);

				stmt.execute();
				try {
					final ResultSet results = stmt.getResultSet();

					try {
						final Set<String> tempIds = new FastSet<String>(WhiteListEnforcer.this.peerIds.size());
	
						while (results.next()) {
							tempIds.add(results.getString(1));
						}
	
						WhiteListEnforcer.this.peerIds = tempIds;
					} finally {
						results.close();
					}
				} finally {
					stmt.close();
				}
			} catch (final SQLException e) {
				LOGGER.error(Exceptions.logInfo(e));
				this.connectionIsValid = false;
			}
		}
	}
}