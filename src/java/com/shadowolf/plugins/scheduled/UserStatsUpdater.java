package com.shadowolf.plugins.scheduled;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.shadowolf.announce.Announce;
import com.shadowolf.plugins.AnnounceFilter;
import com.shadowolf.plugins.ScheduledDBPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.user.User;
import com.shadowolf.user.UserFactory;

public class UserStatsUpdater extends ScheduledDBPlugin implements AnnounceFilter {
	private final static boolean DEBUG = false;
	private final static Logger LOGGER = Logger.getLogger(UserStatsUpdater.class);

	protected transient Set<String> updates = Collections.synchronizedSet(new HashSet<String>());

	private final String table;
	private final String downColumn;
	private final String upColumn;
	private final String passkeyColumn;

	public UserStatsUpdater(final Map<String, String> attributes) {
		this.table = attributes.get("table");
		this.downColumn = attributes.get("down_column");
		this.upColumn = attributes.get("up_column");
		this.passkeyColumn =  attributes.get("passkey_column");

		if(DEBUG) {
			LOGGER.debug("Instatiated UserStatsUpdater plugin.");
		}

	}

	public void run() {
		synchronized (this.updates) {
			if(this.updates.size() == 0) {
				return;
			}
		}


		final PreparedStatement stmt = this.prepareStatement("UPDATE " + this.table + " SET " + this.downColumn + "= " + this.downColumn + "+ ?, "
				+ this.upColumn + "= " + this.upColumn + " + ? WHERE " + this.passkeyColumn + "=?");

		if(stmt == null) {
			LOGGER.error("Could not prepare statement!");
			return;
		}

		try {


			try {
				for(final String passkey : this.updates) {
					if(DEBUG) {
						LOGGER.debug("Updating... " + passkey);
					}

					final User userAggregate = UserFactory.aggregate(passkey);

					stmt.setLong(1, userAggregate.getDownloaded());
					stmt.setLong(2,  userAggregate.getUploaded());
					stmt.setString(3, passkey);
					stmt.executeUpdate();

					userAggregate.resetStats(); //this is necessary in cases where stats do NOT get aggregated
					this.updates.remove(passkey);
				}
			} finally {
				stmt.close();
			}

			this.commit();
		} catch (final SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
			this.rollback();
		}

	}

	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		if((announce.getUploaded() > 0) || (announce.getDownloaded() > 0)) {

			synchronized(this.updates) {
				if(DEBUG) {
					LOGGER.debug("Adding " + announce.getPasskey() + " to update queue.");
				}
				this.updates.add(announce.getPasskey());
			}
		}
	}
}
