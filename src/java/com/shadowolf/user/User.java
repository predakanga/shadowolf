package com.shadowolf.user;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.shadowolf.tracker.Peer;

/*
 * This class is a multiton around itself.  The class doesn't serve much purpose other than to keep track
 * of all Peers that constitute a user, for statistics and access control.
 */
public class User { 
	
	protected ConcurrentHashMap<Long, Peer> peers = new ConcurrentHashMap<Long, Peer>(); // NOPMD by Eddie on 3/6/10 3:32 AM
	
	protected final String peerId;// NOPMD by Eddie on 3/6/10 3:32 AM
	protected String passkey; // NOPMD by Eddie on 3/6/10 3:32 AM
	protected long uploaded = 0L;
	protected long downloaded = 0L;
	protected final Object upLock = new Object();
	protected final Object downLock = new Object();
	
	protected User() {
		//this exists for our child class.
		this.peerId = null;
	}
	
	public User(final String peerId, final String passkey) {
		this.peerId = peerId;
		this.passkey = passkey;
	}
	
	public void addUploaded(int uploaded) {
		this.addUploaded((long) uploaded);
	}
	
	public void addDownloaded(int downloaded) {
		this.addDownloaded((long) downloaded);
	}
	
	public void addUploaded(String uploaded) {
		this.addUploaded(Long.parseLong(uploaded));
	}
	
	public void addDownloaded(String downloaded) {
		this.addDownloaded(Long.parseLong(downloaded));
	}
		
	public void addUploaded(long uploaded) {
		synchronized (this.upLock) {
			this.uploaded += uploaded;
		}
	}
	
	public void addDownloaded(long downloaded) {
		synchronized (this.downLock) {
			this.downloaded += downloaded;
		}
	}
	
	public void updateStats(long infoHash, long uploaded, long downloaded, String ipAddress, String port) throws IllegalAccessException {
		long upDiff; long downDiff;
		
		Peer peer = this.getPeer(infoHash, ipAddress, port);
		
		synchronized (peer) {
			upDiff = uploaded - peer.getUploaded();
			downDiff = downloaded - peer.getDownloaded();
			peer.setDownloaded(downloaded);
			peer.setUploaded(uploaded);
		}
		
		synchronized(this.upLock) {
			this.uploaded += upDiff;
		}
		
		synchronized(this.downLock) {
			this.downloaded += downDiff;
		}
	}
	
	public Peer getPeer(final long infoHash, String ipAddress, String port) throws IllegalAccessException {
		if(this.peers.get(infoHash) == null) {
			Peer p =  new Peer(this.passkey, this.peerId, infoHash, ipAddress, port);
			this.peers.put(infoHash, p);  // NOPMD by Eddie on 3/6/10 3:32 AM
			return p;
		} else {
			return this.peers.get(infoHash);
		}
	}	
	
	
	public long iterateGetDownloaded() {
		long d = 0;
		Iterator<Peer> iter = this.peers.values().iterator();
		
		while(iter.hasNext()) {
			d += iter.next().getDownloaded();
		}
		
		return d;
	}
	
	public long iterateGetUploaded() {
		long d = 0;
		Iterator<Peer> iter = this.peers.values().iterator();
		
		while(iter.hasNext()) {
			d += iter.next().getUploaded();
		}
		
		return d;
	}
	
	public String getPasskey() {
		return this.passkey;
	}
	
	public ConcurrentHashMap<Long, Peer> getPeers() {
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
}
