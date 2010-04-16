package com.shadowolf.plugins.scheduled;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.shadowolf.announce.Announce;
import com.shadowolf.announce.Event;
import com.shadowolf.plugins.AnnounceFilter;
import com.shadowolf.plugins.ScheduledDBPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.util.Data;

/*
 * See the comments on SnatchList.run() for a reason we have this thing NOPMD'd
 */
public class SnatchList extends ScheduledDBPlugin implements AnnounceFilter { //NOPMD
	private final static boolean DEBUG = true;
	private final static Logger LOGGER = Logger.getLogger(SnatchList.class);

	private final String torrentIDColumn; //NOPMD - not a bean
	private final String infoHashColumn;//NOPMD - not a bean
	private final String torrentTable;//NOPMD - not a bean

	private final String passkeyColumn;//NOPMD - not a bean
	private final String userIDColumn;//NOPMD - not a bean
	private final String userTable;//NOPMD - not a bean

	private final String snatchedTable;//NOPMD - not a bean
	private final String snatchedUserID;//NOPMD - not a bean
	private final String snatchedTorrentID;//NOPMD - not a bean
	private final String snatchTimeStamp;//NOPMD - not a bean

	private final Map<String, Set<String>> snatches = //NOPMD - not a bean
		Collections.synchronizedMap(new HashMap<String, Set<String>>());

	public SnatchList(final Map<String, String> attributes) {
		super();
		
		this.torrentIDColumn = attributes.get("torrent_id_column");
		this.infoHashColumn = attributes.get("info_hash_column");
		this.torrentTable = attributes.get("torrent_table");

		this.passkeyColumn = attributes.get("passkey_column");
		this.userIDColumn = attributes.get("user_id_column");
		this.userTable = attributes.get("user_table");

		this.snatchedTable = attributes.get("snatch_table");
		this.snatchedUserID = attributes.get("snatch_user_id");
		this.snatchedTorrentID = attributes.get("snatch_torrent_id");
		this.snatchTimeStamp = attributes.get("snatch_timestamp_column");

	}

	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		if(announce.getEvent() == Event.COMPLETED) {
			if(DEBUG) {
				LOGGER.debug("Found snatch with info_hash: " + announce.getInfoHash() + " and passkey: " + announce.getPasskey());
			}

			if(this.snatches.get(announce.getPasskey()) == null) {
				synchronized(this.snatches) {
					if(this.snatches.get(announce.getPasskey()) == null) { //NOPMD - this is necessary
						this.snatches.put(announce.getPasskey(), new HashSet<String>());
					}
				}
			}

			if(DEBUG) {
				LOGGER.debug("Adding snatch for passey: " + announce.getPasskey() + " with info_hash: " + announce.getInfoHash());
			}

			synchronized(this.snatches) {
				this.snatches.get(announce.getPasskey()).add(announce.getInfoHash());
			}
		}
	}
	
	public int getUserID(final PreparedStatement userLookup, final String passKey) {
		if (DEBUG) {
			LOGGER.debug("Looking up userId for passkey " + passKey);
		}
		
		int userID = -1;
		
		try {
			userLookup.setString(1, passKey);
			final ResultSet result = userLookup.executeQuery(); //NOPMD - it is being checked
				
			try {
				if (result.next()) {
					userID = result.getInt(1);
				} else {
					LOGGER.error("Executed userlookup statement but result had no rows");
				}
			} finally {
				result.close();
			}
		} catch (SQLException e) {
			LOGGER.error("Failed to execute userlookup statement for passkey " + passKey, e);
		}
		
		return userID;
	}
	
	public int getTorrentID(final PreparedStatement torrentLookup, final String ihString, final byte[] infoHash) {
		if (DEBUG) {
			LOGGER.debug("Looking up torrent-id for info_hash "+ihString);
		}
		
		int torrentID = -1;
		
		try {
			torrentLookup.setBytes(1, infoHash);
			final ResultSet result = torrentLookup.executeQuery(); //NOPMD - we are checking .next()
			
			try {
				if (result.next()) {
					torrentID = result.getInt(1);
				} else {
					LOGGER.error("Executed torrentlookup statement but result had no rows");
				}
			} finally {
				result.close();
			}
		} catch (SQLException e) {
			LOGGER.error("Failed to execute torrentlookup statement for infohash " + ihString, e);
		}
		
		return torrentID;
	}

	/*
	 * PMD complains about the cyclic complexity and the N-Path complexity here.
	 * However, without the debug clauses, we're under the treshold levels.
	 * Since those statements would never even get compiled if DEBUG == false;
	 * which it very much should be in a production environment, we leave it be, and NOPMD it.
	 */
	@Override
	public void run() { //NOPMD
		try {
			if(DEBUG) {
				LOGGER.debug("Preparing statements...");
			}
			
			final PreparedStatement userLookup = this.prepareStatement(
					"SELECT " + this.userIDColumn + " FROM " + this.userTable + " WHERE " + this.passkeyColumn + "=? LIMIT 1"
			);
			if (userLookup == null) {
				LOGGER.error("Could not prepare userlookup statement");
				return; //NOPMD
			}
			
			final PreparedStatement torrentLookup = this.prepareStatement(
					"SELECT " + this.torrentIDColumn + " FROM " + this.torrentTable + " WHERE " + this.infoHashColumn + "=? LIMIT 1"
			);
			if (torrentLookup == null) {
				LOGGER.error("Could not prepare torrentlookup statement");
				return; //NOPMD
			}
			
			final PreparedStatement insertStmt = this.prepareStatement(
					"INSERT INTO " + this.snatchedTable + " (" + this.snatchedUserID + ", " + this.snatchedTorrentID +", " + this.snatchTimeStamp + ")" +
					"VALUES (?, ?, UNIX_TIMESTAMP())"
			);
			if (insertStmt == null) {
				LOGGER.error("Could not prepare insertion statement");
				return;
			}

			if(DEBUG) {
				LOGGER.debug("Prepared statement: " + userLookup.toString());
				LOGGER.debug("Prepared statement: " + torrentLookup.toString());
				LOGGER.debug("Prepared statement: " + insertStmt.toString());
			}

			try {
				if(DEBUG) {
					LOGGER.debug("Locking snatches...");
				}

				synchronized(this.snatches) {
					final Iterator<String> passkeys = this.snatches.keySet().iterator();

					while(passkeys.hasNext()) {
						final String pkey = passkeys.next();
						final Set<String> hashes = this.snatches.get(pkey);
						final Iterator<String> iter = hashes.iterator();				
						final int userID = this.getUserID(userLookup, pkey);

						while(iter.hasNext() && userID >= 0) {
							final String ihString = iter.next();
							final byte[] infoHash = Data.hexStringToByteArray(ihString);
							final int torrentID = this.getTorrentID(torrentLookup, ihString, infoHash);
							
							if (torrentID >= 0) {
								if(DEBUG) {
									LOGGER.debug("Inserting snatch with torrentID " + torrentID + " and userID " + userID);
								}
	
								insertStmt.setInt(1, userID);
								insertStmt.setInt(2, torrentID);
								insertStmt.executeUpdate();
							}
						}

					}
					this.snatches.clear();

				}

				this.commit();
			} finally {
				userLookup.close();
				torrentLookup.close();
				insertStmt.close();
			}
		} catch (final SQLException e) {
			LOGGER.error(e.getMessage());
			this.rollback();
		}

	}



}
