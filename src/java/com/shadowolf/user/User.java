package com.shadowolf.user;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


/*
 * This class is a multiton around itself.  The class doesn't serve much purpose other than to keep track
 * of all Peers that constitute a user, for statistics and access control.
 */
public class User {
	private static final Logger LOGGER = Logger.getLogger(User.class);
	private static final boolean DEBUG = false;

	protected ConcurrentHashMap<String, WeakReference<Peer>> peers =
		new ConcurrentHashMap<String, WeakReference<Peer>>(); // NOPMD by Eddie on 3/6/10 3:32 AM

	protected final String peerId;// NOPMD by Eddie on 3/6/10 3:32 AM
	protected String passkey; // NOPMD by Eddie on 3/6/10 3:32 AM
	protected long uploaded = 0L;
	protected long downloaded = 0L;
	protected final Object upLock = new Object();
	protected final Object downLock = new Object();
	protected long lastAccessed;

	protected User() {
		//this exists for our child class.
		this.peerId = null;
	}

	public User(final String peerId, final String passkey) {
		this.peerId = peerId;
		this.passkey = passkey;

		this.lastAccessed = new Date().getTime();
	}

	public void addUploaded(final String uploaded) {
		this.addUploaded(Long.parseLong(uploaded));
	}

	public void addDownloaded(final String downloaded) {
		this.addDownloaded(Long.parseLong(downloaded));
	}

	public void addUploaded(final long uploaded) {
		synchronized (this.upLock) {
			this.uploaded += uploaded;
		}
	}

	public void addDownloaded(final long downloaded) {
		synchronized (this.downLock) {
			this.downloaded += downloaded;
		}
	}

	public void updateStats(final String infoHash, final long uploaded, final long downloaded, final String ipAddress, final String port) throws IllegalAccessException, UnknownHostException, UnsupportedEncodingException {
		long upDiff;
		long downDiff;

		final Peer peer = this.getPeer(infoHash, ipAddress, port);

		synchronized (peer) {
			upDiff = uploaded - peer.getUploaded();
			if(DEBUG) {
				LOGGER.debug("Old upload: " + peer.getUploaded());
				LOGGER.debug("Announce upload: " + uploaded);
				LOGGER.debug("Difference: " + upDiff);
			}

			downDiff = downloaded - peer.getDownloaded();
		}

		peer.setDownloaded(downloaded);
		peer.setUploaded(uploaded);

		this.addDownloaded(downDiff);
		this.addUploaded(upDiff);
	}

	public Peer getPeer(final String infoHash, final String ipAddress, final String port) throws IllegalAccessException, UnknownHostException, UnsupportedEncodingException {
		if((this.peers.get(infoHash) != null) && (this.peers.get(infoHash).get() == null)) {
			//LOGGER.debug("Found weak reference pointing to null!");
			this.peers.remove(infoHash);
		}

		if(this.peers.get(infoHash) == null) {
			final Peer p =  new Peer(0L, 0L, ipAddress, port);
			this.peers.put(infoHash, new WeakReference<Peer>(p));  // NOPMD by Eddie on 3/6/10 3:32 AM
			return p;
		}

		return this.peers.get(infoHash).get();
	}


	public String getPasskey() {
		return this.passkey;
	}

	public ConcurrentHashMap<String, WeakReference<Peer>> getPeers() {
		return this.peers;
	}

	public Long getUploaded() {
		synchronized(this.upLock) {
			return this.uploaded;
		}
	}

	public Long getDownloaded() {
		synchronized(this.downLock) {
			return this.downloaded;
		}
	}

	public void resetStats() {
		synchronized(this.downLock) {
			this.downloaded = 0;
		}

		synchronized(this.upLock) {
			this.uploaded = 0;
		}
	}

	public long getLastAccessed() {
		return this.lastAccessed;
	}

	public void setLastAccessed(final long lastAccessed) {
		this.lastAccessed = lastAccessed;
	}
}
