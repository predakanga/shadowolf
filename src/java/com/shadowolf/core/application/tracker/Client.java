package com.shadowolf.core.application.tracker;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javolution.util.FastMap;

/**
 * Class that represents a client with the given {@link ClientIdentifier}.
 * <br/><br/>
 * A client, in the Shadowolf context, is a particular IP + Port + Passkey combination.
 * 
 * See {@link ClientRegistry} to retrieve a Client.  This class cannot/should not be
 * directly instantiated.
 */
public class Client implements Comparable<Client> {
	//private static final boolean DEBUG = true;
	//private static final Logger LOGGER = Logger.getLogger(Client.class);

	private final ClientIdentifier clientId;
	private final AtomicLong downloaded = new AtomicLong(0);
	private long latestAccess = new Date().getTime();
	private final Map<Integer, Peer> peers =
			new FastMap<Integer, Peer>().shared();
	private final AtomicLong uploaded = new AtomicLong(0);

	Client(final ClientIdentifier clientId) {
		this.clientId = clientId;
	}

	/**
	 * Add downloaded to the current total upload. This will affect the database
	 * stats insertion.
	 */
	public void addDownloaded(final long downloaded) {
		this.downloaded.addAndGet(downloaded);
	}

	/**
	 * Add uploaded to the current total upload. This will affect the database
	 * stats insertion.
	 */
	public void addUploaded(final long uploaded) {
		this.uploaded.addAndGet(uploaded);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Client other = (Client) obj;
		if (this.clientId == null) {
			if (other.clientId != null) {
				return false;
			}
		} else if (!this.clientId.equals(other.clientId)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the total amount of downloadfor this client.
	 */
	public long getDownloaded() {
		return this.downloaded.longValue();
	}

	/**
	 * Returns the latest access time, as a unix timestamp (generated from
	 * {@link Date#getTime()}). Used for cleanup / expiry of dead clients.
	 */
	public long getLatestAccess() {
		return this.latestAccess;
	}

	/**
	 * Returns true if this Client has a {@link Peer} to the torrent with
	 * ID <i>torrentId</i>.
	 */
	public boolean hasTorrent(final Integer torrentId) {
		return this.peers.containsKey(torrentId);
	}
	
	/**
	 * Returns a peer for the torrent with ID <i>torrentId</i> and creates one,
	 * and adds it to the internal peer list if it doesn't exist.  If you just
	 * want to determine if a client has a record for a peer or not, use {@link #hasTorrent(Integer)}.
	 * @return the Peer instance, or a new one.
	 */
	public Peer getPeer(final Integer torrentId) {
		this.setLatestAccess();
		Peer peer = this.peers.get(torrentId);

		if (peer != null) {
			peer.setLatestAnnounce(this.getLatestAccess());
			return peer;
		}

		peer = new Peer();
		peer.setTorrentId(torrentId);

		this.peers.put(torrentId, peer);

		return peer;

	}

	/**
	 * return an immutable copy of all the peers this client has.
	 */
	public Map<Integer, Peer> getPeers() {
		return Collections.unmodifiableMap(this.peers);
	}

	/**
	 * Returns the total amount of upload for this client.
	 */
	public long getUploaded() {
		return this.uploaded.longValue();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.clientId == null) ? 0 : this.clientId.hashCode());
		return result;
	}

	/**
	 * Removes a peer of the torrent with the given torrent ID.
	 * 
	 * @param torrentId
	 *            the torrent ID to remove a peer of.
	 */
	public void removePeer(final Integer torrentId) {
		this.peers.remove(torrentId);
	}

	/**
	 * Removes a {@link Peer} from this client.
	 * 
	 * @param peer
	 *            the Peer to remove.
	 */
	public void removePeer(final Peer peer) {
		this.peers.remove(peer.getTorrentId());
	}

	/**
	 * Updates the latest access time for this client to the curent time.
	 */
	public void setLatestAccess() {
		this.latestAccess = new Date().getTime();
	}

	/**
	 * Updates this client's stats (and the Peer it wraps)
	 * 
	 * @param torrentId
	 *            the torrentId of the stats to update
	 * @param uploaded
	 *            the new upload total
	 * @param downloaded
	 *            the new download total
	 */
	public void updateStats(final Integer torrentId, final long uploaded, final long downloaded) {
		this.setLatestAccess();

		final Peer peer = this.getPeer(torrentId);

		synchronized (peer) {
			final long upDelta = uploaded - peer.getUploaded();
			final long downDelta = downloaded - peer.getDownloaded();

			/*
			 * The upDelta (the difference in the current announced upload value
			 * and the stored) will be less than 0 in a situation in which we
			 * missed the STOPPED event and the client started the torrent again
			 * and uploaded more. In the event that we miss the stopped event
			 * and the updelta is still >0, there's no way to catch that and
			 * it'll be a known issue that we can't logistically fix, because
			 * the state transfer doesn't give us enough infomation to realize
			 * what happened.
			 */
			if (upDelta < 0) {
				this.addUploaded(uploaded);
			} else {
				this.addUploaded(upDelta);
			}
			peer.setUploaded(uploaded);

			// justification for this check is same as uploaded
			if (downDelta < 0) {
				this.addDownloaded(downloaded);
			} else {
				this.addDownloaded(upDelta);
			}
			peer.setDownloaded(downloaded);
		}
	}

	@Override
	public int compareTo(Client o) {
		if(this.getLatestAccess() > o.getLatestAccess()) {
			return -1; //o is "older" aka "first"
		} else if (this.getLatestAccess() < o.getLatestAccess()) {
			return 1;
		} else {
			return 0;
		}
	}

}
