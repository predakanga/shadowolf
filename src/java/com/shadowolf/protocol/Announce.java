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
	
	private long upDelta;
	private long downDelta;
	private long timeDelta;
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

	public long getUpDelta() {
		return upDelta;
	}

	public void setUpDelta(long upDelta) {
		this.upDelta = upDelta;
	}

	public long getDownDelta() {
		return downDelta;
	}

	public void setDownDelta(long downDelta) {
		this.downDelta = downDelta;
	}

	public long getTimeDelta() {
		return timeDelta;
	}

	public void setTimeDelta(long timeDelta) {
		this.timeDelta = timeDelta;
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