package com.shadowolf.plugins.scheduled;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javolution.util.FastComparator;
import javolution.util.FastSet;
import javolution.util.FastCollection.Record;

import org.apache.log4j.Logger;

import com.shadowolf.announce.Announce;
import com.shadowolf.announce.Event;
import com.shadowolf.plugins.AnnounceAction;
import com.shadowolf.plugins.ScheduledDBPlugin;
import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.util.Data;
import com.shadowolf.util.Exceptions;

public class SnatchList extends ScheduledDBPlugin implements AnnounceAction {
	private final static boolean DEBUG = true;
	private final static Logger LOGGER = Logger.getLogger(SnatchList.class);

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

	private volatile FastSet<Snatch> snatches;

	public SnatchList(final Map<String, String> attributes) {
		super();

		this.snatches = new FastSet<Snatch>();
		this.snatches.shared();
		this.snatches.setValueComparator(new SnatchComparator());

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

			this.snatches.add(new Snatch(announce.getPasskey(), announce.getInfoHash()));

			if(DEBUG) {
				LOGGER.debug("Snatches size: " + this.snatches.size());
			}
		}
	}

	@Override
	public void run() {
		if(DEBUG) {
			LOGGER.debug("Running snatchlist... ");
		}

		if(this.snatches.isEmpty()) {
			return;
		}

		final FastSet<Snatch> oldList = this.snatches;
		this.snatches = new FastSet<Snatch>();
		this.snatches.shared();
		
		try {

			final TorrentIDLookup torrentLookup = new TorrentIDLookup();
			final UserIDLookup userLookup = new UserIDLookup();
			final SnatchInserter snatchInsert = new SnatchInserter();

			try {
				for(Record snatch = oldList.head(), end = oldList.tail(); (snatch = snatch.getNext()) != end;) {
					final int torrentId = torrentLookup.lookup(oldList.valueOf(snatch).getInfoHash());
					final int userId = userLookup.lookup(oldList.valueOf(snatch).getPasskey());

					snatchInsert.insert(torrentId, userId);
				}

			} finally {
				this.commit();
				torrentLookup.close();
				userLookup.close();
				snatchInsert.close();
			}
		} catch (final SQLException e) {
			LOGGER.error(Exceptions.logInfo(e));
		}

	}

	private class SnatchInserter {
		final PreparedStatement stmt;

		public SnatchInserter() {
			this.stmt = SnatchList.this.prepareStatement(
					"INSERT INTO " + SnatchList.this.snatchedTable + " (" + SnatchList.this.snatchedUserID + ", " + SnatchList.this.snatchedTorrentID +", " + SnatchList.this.snatchTimeStamp + ")" +
					"VALUES (?, ?, UNIX_TIMESTAMP())"
			);
		}

		public void close() throws SQLException {
			this.stmt.close();
		}

		public void insert(final int torrentId, final int userId) throws SQLException {
			this.stmt.setInt(1, userId);
			this.stmt.setInt(2, torrentId);
			this.stmt.executeUpdate();
		}
	}

	private class TorrentIDLookup {
		final PreparedStatement stmt;

		public TorrentIDLookup() {
			this.stmt = SnatchList.this.prepareStatement(
					"SELECT " + SnatchList.this.torrentIDColumn + " FROM " + SnatchList.this.torrentTable + " WHERE " + SnatchList.this.infoHashColumn + "=? LIMIT 1"
			);
		}

		public void close() throws SQLException {
			this.stmt.close();
		}

		public int lookup(final String infoHash) throws SQLException {
			InputStream infoStream = new ByteArrayInputStream(Data.hexStringToByteArray(infoHash));
			this.stmt.setBinaryStream(1, infoStream, 20);
			this.stmt.execute();
			final ResultSet result = this.stmt.getResultSet();

			int torrentId = -1;

			try {
				if(result.next()) {
					torrentId = result.getInt(1);
				} else {
					LOGGER.error("Executed infoHash statement but result had no rows for info_hash: " + infoHash);
				}
			} finally {
				result.close();
			}

			return torrentId;
		}
	}

	private class UserIDLookup {
		final PreparedStatement stmt;

		public UserIDLookup() {
			this.stmt = SnatchList.this.prepareStatement(
					"SELECT " + SnatchList.this.userIDColumn + " FROM " + SnatchList.this.userTable + " WHERE " + SnatchList.this.passkeyColumn + "=? LIMIT 1"
			);
		}

		public void close() throws SQLException {
			this.stmt.close();
		}

		public int lookup(final String passkey) throws SQLException {
			this.stmt.setString(1, passkey);
			this.stmt.execute();
			final ResultSet result = this.stmt.getResultSet();

			int userId = -1;

			try {
				if(result.next()) {
					userId = result.getInt(1);
				} else {
					LOGGER.error("Executed userlookup statement but result had no rows for passkey: " + passkey);
				}
			} finally {
				result.close();
			}

			return userId;
		}
	}

	private class Snatch {
		private final String passkey;
		private final String infoHash;

		public String getPasskey() {
			return this.passkey;
		}

		public String getInfoHash() {
			return this.infoHash;
		}

		public Snatch(final String passkey, final String infoHash) {
			this.passkey = passkey;
			this.infoHash = infoHash;
		}
	}

	private class SnatchComparator extends FastComparator<Snatch> {
		private static final long serialVersionUID = -4090135208524169093L;

		@Override
		public boolean areEqual(final Snatch o1, final Snatch o2) {
			return o1.getPasskey().equals(o2.getPasskey()) && o2.getInfoHash().equals(o2.getInfoHash());
		}

		@Override
		public int compare(final Snatch o1, final Snatch o2) {
			if(this.hashCodeOf(o1) < this.hashCodeOf(o2)) {
				return -1;
			} else if(this.hashCodeOf(o1) == this.hashCodeOf(o2)) {
				return 0;
			} else {
				return 1;
			}
		}

		@Override
		public int hashCodeOf(final Snatch obj) {
			final StringBuilder hash = new StringBuilder(obj.getInfoHash());
			hash.append(obj.getPasskey());
			return hash.toString().hashCode();
		}

	}
}
