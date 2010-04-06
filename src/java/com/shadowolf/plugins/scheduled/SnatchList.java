package com.shadowolf.plugins.scheduled;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.shadowolf.announce.Announce;
import com.shadowolf.plugins.AnnounceFilter;
import com.shadowolf.plugins.ScheduledPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerRequest.Event;
import com.shadowolf.util.Data;

public class SnatchList extends ScheduledPlugin implements AnnounceFilter {
	private final static boolean DEBUG = true;
	private final static Logger LOGGER = Logger.getLogger(SnatchList.class);
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";

	private final String torrentIDColumn;
	private final String infoHashColumn;
	private final String torrentTable;

	private final String passkeyColumn;
	private final String userIDColumn;
	private final String userTable;

	private final String snatchedTable;
	private final String snatchedUserID;
	private final String snatchedTorrentID;
	private final String snatchTimeStamp;

	private final Map<String, Set<String>> snatches =
		Collections.synchronizedMap(new HashMap<String, Set<String>>());

	private Connection connection;

	public SnatchList(final Attributes attributes) {
		super(attributes);

		this.torrentIDColumn = attributes.getValue("torrent_id_column");
		this.infoHashColumn = attributes.getValue("info_hash_column");
		this.torrentTable = attributes.getValue("torrent_table");

		this.passkeyColumn = attributes.getValue("passkey_column");
		this.userIDColumn = attributes.getValue("user_id_column");
		this.userTable = attributes.getValue("user_table");

		this.snatchedTable = attributes.getValue("snatch_table");
		this.snatchedUserID = attributes.getValue("snatch_user_id");
		this.snatchedTorrentID = attributes.getValue("snatch_torrent_id");
		this.snatchTimeStamp = attributes.getValue("snatch_timestamp_column");

	}

	@Override
	public void doAnnounce(final Announce announce) throws AnnounceException {
		if(announce.getEvent() == Event.COMPLETED) {
			if(DEBUG) {
				LOGGER.debug("Found snatch with info_hash: " + announce.getInfoHash() + " and passkey: " + announce.getPasskey());
			}
			
			if(this.snatches.get(announce.getPasskey()) == null) {
				synchronized(this.snatches) {
					if(this.snatches.get(announce.getPasskey()) == null) {
						this.snatches.put(announce.getPasskey(), Collections.synchronizedSet(new HashSet<String>()));
					}
				}
			}

			if(DEBUG) {
				LOGGER.debug("Adding snatch for passey: " + announce.getPasskey() + " with info_hash: " + announce.getInfoHash());
			}

			synchronized(this.snatches.get(announce.getPasskey())) {
				this.snatches.get(announce.getPasskey()).add(announce.getInfoHash());
			}
		}
	}

	public void run() {
		try {
			if(DEBUG) {
				LOGGER.debug("Preparing statement...");
			}
			
			final PreparedStatement stmt = this.prepareStatement();

			if(DEBUG) {
				LOGGER.debug("Prepared statement: " + stmt.toString());
			}
			
			try {
				if(DEBUG) {
					LOGGER.debug("Locking snatches...");
				}
				synchronized(this.snatches){
					final Iterator<String> passkeys = this.snatches.keySet().iterator();

					while(passkeys.hasNext()) {
						final String pkey = passkeys.next();
						final Set<String> hashes = this.snatches.get(pkey);
						final Iterator<String> iter = hashes.iterator();

						
						
						while(iter.hasNext()) {
							final String ihString = iter.next();
							final byte[] infoHash = Data.hexStringToByteArray(ihString);
							
							if(DEBUG) {
								LOGGER.debug("Inserting snatch with info_hash: " + ihString + " and passkey: " + pkey);
							}
							
							stmt.setString(1, pkey);
							stmt.setBytes(2, infoHash);
							stmt.executeUpdate();
						}

					}
					this.snatches.clear();
					
				}

				this.connection.commit();
			} finally {
				stmt.close();
			}
		} catch (final SQLException e) {
			LOGGER.error(e.getMessage());
		}

	}
	private PreparedStatement prepareStatement() {
		if(DEBUG) {
			LOGGER.debug("Opening connection...");
		}
		
		
		this.openConnection();

		if(DEBUG) {
			LOGGER.debug("Opened connection.");
		}
		
		try {
			return this.connection.prepareStatement(
					"INSERT INTO " + this.snatchedTable + " (" + this.snatchedUserID + ", " + this.snatchedTorrentID +", " + this.snatchTimeStamp + ")" +
					"VALUES ((SELECT " + this.userIDColumn + " FROM " + this.userTable + " WHERE " + this.passkeyColumn + "=? LIMIT 1)," +
					"(SELECT " + this.torrentIDColumn + " FROM " + this.torrentTable + " WHERE " + this.infoHashColumn + "=? LIMIT 1)," +
					"UNIX_TIMESTAMP())"
			);
		} catch (final SQLException e) {
			LOGGER.error(e.getMessage());
			
			if(e.getCause() != null) {
				LOGGER.error("Cause error: " + e.getCause().getMessage());
			}
			
			return null;
		}
	}

	private void openConnection() {
		try {
			if(this.connection == null) {
				final DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
				this.connection = source.getConnection();
				this.connection.setAutoCommit(false);
			}else {
				LOGGER.error("Connection already established!");
			}
		} catch (final Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
