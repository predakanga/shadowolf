package com.shadowolf.core.application.tracker;

import java.io.Serializable;
import java.util.Date;

/**
 * Javabean that represents a peer.  Fields are self-explanatory.
 */
public class Peer implements Serializable {
	private static final long serialVersionUID = -3268759338529305082L;

	private long uploaded = 0L;
	private long downloaded = 0L;
	private long latestAnnounce;
	private int torrentId;
	private ClientIdentifier clientIdentifier;
	
	public Peer() {
		this.latestAnnounce = new Date().getTime();
	}
	
	public long getDownloaded() {
		return this.downloaded;
	}

	public long getLatestAnnounce() {
		return this.latestAnnounce;
	}

	public int getTorrentId() {
		return this.torrentId;
	}

	public long getUploaded() {
		return this.uploaded;
	}

	public void setDownloaded(long downloaded) {
		this.downloaded = downloaded;
	}

	public void setLatestAnnounce(long latestAnnounce) {
		this.latestAnnounce = latestAnnounce;
	}

	public void setTorrentId(int torrentId) {
		this.torrentId = torrentId;
	}

	public void setUploaded(long uploaded) {
		this.uploaded = uploaded;
	}

	public void setClientIdentifier(ClientIdentifier clientIdentifier) {
		this.clientIdentifier = clientIdentifier;
	}

	public ClientIdentifier getClientIdentifier() {
		return clientIdentifier;
	}

}
