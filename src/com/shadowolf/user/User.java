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
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, User>> users = new ConcurrentHashMap<String, ConcurrentHashMap<String,User>>(1024, 0.66F, 24); 
	
	protected ConcurrentHashMap<Long, Peer> peers = new ConcurrentHashMap<Long, Peer>(); // NOPMD by Eddie on 3/6/10 3:32 AM
	
	private final String peerId;// NOPMD by Eddie on 3/6/10 3:32 AM
	private final String passkey; // NOPMD by Eddie on 3/6/10 3:32 AM
	private Long uploaded = new Long(0);
	private Long downloaded = new Long(0);
	
	public User() throws InstantiationError {
		throw new InstantiationError("Cannot instantiate User class!");
	}
	
	private User(final String peerId, final String passkey) {
		this.peerId = peerId;
		this.passkey = passkey;
	}
	
	public static User getUser(final String peerId, final String passkey) {
		User u;
		if(users.containsKey(passkey)) {
			if((u = users.get(passkey).get(peerId)) != null) {
				return u;
			} else {
				u = new User(peerId, passkey);
				return users.get(passkey).put(peerId, u);
			}
		} else {
			u = new User(peerId, passkey);
			users.put(passkey, new ConcurrentHashMap<String, User>(3, 1.0F, 4));
			return users.get(passkey).put(peerId, u);
		}
		
	}
	
	public synchronized UserAggregate aggregate() {
		Iterator<User> iter = users.get(passkey).values().iterator();
		
		UserAggregate user = new UserAggregate();
		
		while(iter.hasNext()) {
			final User u = iter.next();
			user.addPeerlist(u.getPeers());
		}
		
		return user;
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
