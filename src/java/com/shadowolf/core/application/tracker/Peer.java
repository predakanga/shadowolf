package com.shadowolf.core.application.tracker;

import java.io.Serializable;
import java.util.Date;

/**
 * Javabean that represents a peer.  Fields are self-explanatory.
 * <br/><br/>
 * A peer in the SW sense is just a tracking of upload, download, latest announce and a torrent.
 * The actual "peer" in the typical bittorrent sense is a ClientIdentifier instance.
 */
public class Peer implements Serializable {
	private static final long serialVersionUID = -3268759338529305082L;

	private long uploaded = 0L;
	private long downloaded = 0L;
	private long latestAnnounce;
	private int torrentId;
	
	public Peer() {
		this.latestAnnounce = new Date().getTime();
	}
	
	/**
	 * Get the amount of download on this torrent.
	 */
	public long getDownloaded() {
		return this.downloaded;
	}

	/**
	 * Get the latest announce in the same format returned by {@link Date#getTime()}.
	 * @see Date#getTime()
	 */
	public long getLatestAnnounce() {
		return this.latestAnnounce;
	}

	/**
	 * Get the torrent ID for this peer
	 */
	public int getTorrentId() {
		return this.torrentId;
	}

	/**
	 * Get the amount of upload for this peer
	 */
	public long getUploaded() {
		return this.uploaded;
	}

	/**
	 * Set the peer download to the specified value
	 */
	public void setDownloaded(long downloaded) {
		this.downloaded = downloaded;
	}

	/**
	 * Set the lastest annoucne time
	 */
	public void setLatestAnnounce(long latestAnnounce) {
		this.latestAnnounce = latestAnnounce;
	}

	/**
	 * Set the torrent ID
	 */
	public void setTorrentId(int torrentId) {
		this.torrentId = torrentId;
	}

	/**
	 * Set the peer upload to the specified value
	 */
	public void setUploaded(long uploaded) {
		this.uploaded = uploaded;
	}


}
