package com.shadowolf.user;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.shadowolf.tracker.Peer;

/*
 * This class is a multiton around itself.  The class doesn't serve much purpose other than to keep track
 * of all Peers that constitute a user, for statistics and access control.
 */
//Sort-of PMD compliant.
public class User { 
	
	protected ConcurrentHashMap<Long, Peer> peers = new ConcurrentHashMap<Long, Peer>(); // NOPMD by Eddie on 3/6/10 3:32 AM
	
	protected final String peerId;// NOPMD by Eddie on 3/6/10 3:32 AM
	protected String passkey; // NOPMD by Eddie on 3/6/10 3:32 AM
	protected Long uploaded = new Long(0);
	protected Long downloaded = new Long(0);
	
	protected User() {
		//this exists for our child class.
		this.peerId = null;
	}
	
	public User(final String peerId, final String passkey) {
		this.peerId = peerId;
		this.passkey = passkey;
	}
	
	public void updateStats(String infoHash, long uploaded, long downloaded) {
		long upDiff; long downDiff;
		
		Peer peer = this.peers.get(infoHash);
		
		synchronized (peer) {
			upDiff = uploaded - peer.getUploaded();
			downDiff = downloaded - peer.getDownloaded();
			peer.setDownloaded(downloaded);
			peer.setUploaded(uploaded);
		}
		
		synchronized(this.uploaded) {
			this.uploaded += upDiff;
		}
		
		synchronized(this.downloaded) {
			this.downloaded += downDiff;
		}
	}
	
	public Peer getPeer(final long infoHash) {
		if(this.peers.containsKey(infoHash)) {
			return this.peers.put(infoHash, new Peer(this.passkey, this.peerId, infoHash));  // NOPMD by Eddie on 3/6/10 3:32 AM
		} else {
			return this.peers.get(infoHash);
		}
	}	
	
	
	public double getDownloaded() {
		double d = 0;
		Iterator<Peer> iter = this.peers.values().iterator();
		
		synchronized(this.peers) {
			while(iter.hasNext()) {
				d += iter.next().getDownloaded();
			}
		}
		
		return d;
	}
	
	public double getUploaded() {
		double d = 0;
		Iterator<Peer> iter = this.peers.values().iterator();
		
		synchronized(this.peers) {
			while(iter.hasNext()) {
				d += iter.next().getUploaded();
			}
		}
		
		return d;
	}
	
	public String getPasskey() {
		return this.passkey;
	}
	
	public ConcurrentHashMap<Long, Peer> getPeers() {
		return this.peers;
	}
}
