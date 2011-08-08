package com.shadowolf.protocol;

import java.net.InetSocketAddress;

/**
 * This represents the actual data of an announce. It stores the Infohash,
 * passkey, peerId, event, the number of peers desired, as well as changes 
 * in upload, download, and time since the last announce. This object
 * is fully mutable until unmutable() is called, after which no modifications
 * will be prmitted, and will throw an IllegalStateException. if attempted.
 * <br /><br />
 * <strong>This class is only threadsafe after cast to Announce!</strong>
 */
public class AnnounceImpl implements Announce {
	
	private Infohash hash;
	private String passkey;
	private String peerId;
	private Event event;
	
	private long upDelta;
	private long downDelta;
	private int timeDelta;
	private long left;
	private int numwant;
	
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

	public com.shadowolf.protocol.Announce.Event getEvent() {
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

	public void setTimeDelta(int timeDelta) {
		this.timeDelta = timeDelta;
	}

	public long getLeft() {
		return left;
	}

	public void setLeft(long left) {
		this.left = left;
	}

	public int getNumwant() {
		return numwant;
	}

	public void setNumwant(int numwant) {
		this.numwant = numwant;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}
}