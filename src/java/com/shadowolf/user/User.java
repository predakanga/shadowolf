package com.shadowolf.user;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.shadowolf.config.Config;


/*
 * This class is a multiton around itself.  The class doesn't serve much purpose other than to keep track
 * of all Peers that constitute a user, for statistics and access control.
 */
public class User {
	private static final Logger LOGGER = Logger.getLogger(User.class);
	private static final boolean DEBUG = false;

	protected ConcurrentHashMap<String, WeakReference<Peer>> peers =
		new ConcurrentHashMap<String, WeakReference<Peer>>();

	protected final String peerId;
	protected String passkey; 
	protected long uploaded = 0L;
	protected long downloaded = 0L;
	protected final Object upLock = new Object();
	protected final Object downLock = new Object();
	protected long lastAccessed;

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

			if(downDiff < 0) {
				this.addDownloaded(downloaded);
				peer.setDownloaded(downloaded);
			} else {
				this.addUploaded(downDiff);
				peer.setDownloaded(downloaded);
			}

			if(upDiff < 0) {
				this.addUploaded(uploaded);
				peer.setUploaded(uploaded);
			} else {
				this.addDownloaded(upDiff);
				peer.setUploaded(uploaded);
			}
		}
	}

	public void removePeer(final Peer peer) {
		this.peers.remove(peer.getInfoHash());
	}

	public void removePeer(final String infoHash) {
		this.peers.remove(infoHash);
	}

	public Peer getPeer(final String infoHash, final String ipAddress, final String port) throws IllegalAccessException, UnknownHostException, UnsupportedEncodingException {
		if((this.peers.get(infoHash) != null) && (this.peers.get(infoHash).get() == null)) {
			//LOGGER.debug("Found weak reference pointing to null!");
			this.peers.remove(infoHash);
		}

		if(this.peers.get(infoHash) == null) {
			final Peer p =  new Peer(0L, 0L, ipAddress, port, infoHash);
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

	public Long resetUploaded() {
		synchronized(this.upLock) {
			return this.uploaded;
		}
	}

	public Long resetDownloaded() {
		synchronized(this.downLock) {
			return this.downloaded;
		}
	}

	public long getLastAccessed() {
		return this.lastAccessed;
	}

	public void setLastAccessed(final long lastAccessed) {
		this.lastAccessed = lastAccessed;
	}

	private static final User emptyInst = new User(null, null) {
		@Override
		public void addUploaded(final String uploaded) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addDownloaded(final String downloaded) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addUploaded(final long uploaded) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addDownloaded(final long downloaded) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void updateStats(final String infoHash, final long uploaded, final long downloaded, final String ipAddress, final String port) throws IllegalAccessException, UnknownHostException, UnsupportedEncodingException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Peer getPeer(final String infoHash, final String ipAddress, final String port) throws IllegalAccessException, UnknownHostException, UnsupportedEncodingException {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getPasskey() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ConcurrentHashMap<String, WeakReference<Peer>> getPeers() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Long resetUploaded() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Long resetDownloaded() {
			throw new UnsupportedOperationException();
		}
	};

	public static User getEmptyInstance() {
		emptyInst.setLastAccessed(new Date().getTime() + (Integer.parseInt(Config.getParameter("peer.timeout")) * 1000));
		return emptyInst;
	}
}
