package com.shadowolf.plugins.scheduled;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import javolution.util.FastSet;

import org.apache.log4j.Logger;

import com.shadowolf.announce.Announce;
import com.shadowolf.plugins.AnnounceAction;
import com.shadowolf.plugins.ScheduledDBPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.user.User;
import com.shadowolf.user.UserFactory;

public class UserStatsUpdater extends ScheduledDBPlugin implements AnnounceAction {
	private final static boolean DEBUG = false;
	private final static Logger LOGGER = Logger.getLogger(UserStatsUpdater.class);

	protected FastSet<String> updates;

	private final String table;
	private final String downColumn;
	private final String upColumn;
	private final String passkeyColumn;

	public UserStatsUpdater(final Map<String, String> attributes) {
		this.table = attributes.get("table");
		this.downColumn = attributes.get("down_column");
		this.upColumn = attributes.get("up_column");
		this.passkeyColumn =  attributes.get("passkey_column");

		this.updates = new FastSet<String>();
		this.updates.shared();
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

		FastSet<String> tempUpdates = this.updates;
		
		this.updates = new FastSet<String>();
		this.updates.shared();
		
		final PreparedStatement stmt = this.prepareStatement("UPDATE " + this.table + " SET " + this.downColumn + "= " + this.downColumn + "+ ?, "
				+ this.upColumn + "= " + this.upColumn + " + ? WHERE " + this.passkeyColumn + "=?");

		if(stmt == null) {
			LOGGER.error("Could not prepare statement!");
			return;
		}

		try {
			try {
				for(final String passkey : tempUpdates) {
					final User userAggregate = UserFactory.aggregate(passkey);
					
					if(DEBUG) {
						//LOGGER.debug("Updating " + passkey + " with upload: " + userAggregate.getUploaded() + " and download: " + userAggregate.getDownloaded());
					}

					stmt.setLong(1, userAggregate.resetDownloaded());
					stmt.setLong(2,  userAggregate.resetUploaded());
					stmt.setString(3, passkey);
					stmt.executeUpdate();

					//userAggregate.resetStats(); //this is necessary in cases where stats do NOT get aggregated
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
