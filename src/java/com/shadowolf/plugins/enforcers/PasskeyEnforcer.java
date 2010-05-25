package com.shadowolf.plugins.enforcers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import javolution.util.FastSet;

import org.apache.log4j.Logger;

import com.shadowolf.core.application.announce.Announce;
import com.shadowolf.core.application.announce.AnnounceException;
import com.shadowolf.core.application.plugin.AbstractScheduledPlugin;
import com.shadowolf.core.application.plugin.AnnounceFilter;
import com.shadowolf.core.application.tracker.response.Errors;
import com.shadowolf.core.database.DatabaseWrapper;
import com.shadowolf.util.Exceptions;

public class PasskeyEnforcer extends AbstractScheduledPlugin implements AnnounceFilter {
	private final static Logger LOGGER = Logger.getLogger(PasskeyEnforcer.class);
	private final static boolean DEBUG = true;

	private final PasskeyRetriever retriever;
	private Set<String> passkeys = new FastSet<String>();

	public PasskeyEnforcer(final Map<String, String> options, final ServletContext context) {
		super(options);

		final String table = options.get("table");
		final String column = options.get("passkey_column");

		if ((table == null) || (column == null)) {
			throw new RuntimeException("Missing config parameter for PasskeyEnforcer.");
		}

		final StringBuilder query = new StringBuilder("SELECT ");
		query.append(column);
		query.append(" FROM ");
		query.append(table);

		this.retriever = new PasskeyRetriever(query.toString());
	}

	@Override
	public void filterAnnounce(final Announce announce) throws AnnounceException {
		if (!this.passkeys.contains(announce.getPasskey())) {
			throw new AnnounceException(Errors.INVALID_PASSKEY);
		}

	}

	@Override
	public void init() {
		this.retriever.fetch();
	}

	@Override
	public void destroy() {

	}

	@Override
	public void run() {
		this.retriever.fetch();
	}

	private class PasskeyRetriever extends DatabaseWrapper {
		private final String query;

		public PasskeyRetriever(final String query) {
			this.query = query;
		}

		public void fetch() {
			if(DEBUG) {
				LOGGER.debug("Starting passkey fetch.");
			}
			try {
				final PreparedStatement stmt = this.prepareStatement(this.query);
				final Set<String> newPasskeys = new FastSet<String>(PasskeyEnforcer.this.passkeys.size());
				try {
					stmt.execute();

					final ResultSet results = stmt.getResultSet();

					while (results.next()) {
						newPasskeys.add(results.getString(1));
					}

					PasskeyEnforcer.this.passkeys = newPasskeys;
				} finally {
					stmt.close();
				}
				
				if(DEBUG) {
					LOGGER.debug("Succesfully finished passkey fetch. Found passkeys: " + newPasskeys.size());
				}
				
			} catch (final SQLException e) {
				LOGGER.error(Exceptions.logInfo(e));
				this.connectionIsValid = false;
				throw new RuntimeException("Problem fetching passkeys, see log");
			}

		}
	}

}
