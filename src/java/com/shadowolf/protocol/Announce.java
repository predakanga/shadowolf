package com.shadowolf.protocol;

import java.net.InetSocketAddress;

/**
 * POJO with fields that represent an announce.
 */
public class Announce {
	public static enum Event {
		STARTED,
		STOPPED,
		ANNOUNCE,
		COMPLETE
	};
	
	private Infohash hash;
	private String passkey;
	private String peerId;
	private Event event;
	
	private long uploaded;
	private long downloaded;
	private long left;
	private long numwant;
	
	private InetSocketAddress address;

	public Infohash getHash() {
		return hash;
	}

	public void setHash(Infohash hash) {
		this.hash = hash;
	}

	public String getPasskey() {
		return passkey;
	}

	public void setPasskey(String passkey) {
		this.passkey = passkey;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public long getUploaded() {
		return uploaded;
	}

	public void setUploaded(long uploaded) {
		this.uploaded = uploaded;
	}

	public long getDownloaded() {
		return downloaded;
	}

	public void setDownloaded(long downloaded) {
		this.downloaded = downloaded;
	}

	public long getLeft() {
		return left;
	}

	public void setLeft(long left) {
		this.left = left;
	}

	public long getNumwant() {
		return numwant;
	}

	public void setNumwant(long numwant) {
		this.numwant = numwant;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}
	
}
