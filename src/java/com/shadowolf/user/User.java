package com.shadowolf.user;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;

import com.shadowolf.tracker.Peer;

/*
 * This class is a multiton around itself.  The class doesn't serve much purpose other than to keep track
 * of all Peers that constitute a user, for statistics and access control.
 */
public class User { 
	
	protected HashMap<byte[], Peer> peers = 
		new HashMap<byte[], Peer>(); // NOPMD by Eddie on 3/6/10 3:32 AM
	
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
	
	public void updateStats(byte[] infoHash, long uploaded, long downloaded, String ipAddress, String port) throws IllegalAccessException, UnknownHostException, UnsupportedEncodingException {
		long upDiff; 
		long downDiff;
		
		Peer peer = this.getPeer(infoHash, ipAddress, port);
		
		synchronized (peer) {
			upDiff = uploaded - peer.getUploaded();
			downDiff = downloaded - peer.getDownloaded();
			//peer.setDownloaded(downloaded);
			//peer.setUploaded(uploaded);
		}
		
		this.addDownloaded(downDiff);
		this.addUploaded(upDiff);
	}
	
	public Peer getPeer(final byte[] infoHash, String ipAddress, String port) throws IllegalAccessException, UnknownHostException, UnsupportedEncodingException {
		synchronized(this.peers){ 
			if(this.peers.get(infoHash) == null) {
				Peer p =  new Peer(0L, 0L, ipAddress, port);
				synchronized(this.peers) {
					this.peers.put(infoHash, p);  // NOPMD by Eddie on 3/6/10 3:32 AM
				}
			}
		}
		
		synchronized(this.peers) { 
			return this.peers.get(infoHash);
		}
	}	
	
	
	public long iterateGetDownloaded() {
		long d = 0;
		synchronized(this.peers) {
			Iterator<Peer> iter = this.peers.values().iterator();
			
			while(iter.hasNext()) {
				d += iter.next().getDownloaded();
			}
		}
		
		return d;
	}
	
	public long iterateGetUploaded() {
		long d = 0;
		synchronized(this.peers) {
			Iterator<Peer> iter = this.peers.values().iterator();
			
			while(iter.hasNext()) {
				d += iter.next().getUploaded();
			}
		}	
		return d;
	}
	
	public String getPasskey() {
		return this.passkey;
	}
	
	public HashMap<byte[], Peer> getPeers() {
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
