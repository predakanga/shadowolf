package com.shadowolf.protocol;

import java.net.InetSocketAddress;

/**
 * This represents the actual data of an announce. It stores the Infohash,
 * passkey, peerId, event, the number of peers desired, as well as changes 
 * in upload, download, and time since the last announce. This object
 * is fully mutable until unmutable() is called, after which no modifications
 * will be prmitted, and will throw an IllegalStateException. if attempted.
 * <br /><br />
 * <strong>This class is only threadsafe after unmutable() is called!</strong>
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
	private int timeDelta;
	private long left;
	private int numwant;
	
	private InetSocketAddress address;
	
	private boolean mutable;

	public Infohash getHash() {
		return hash;
	}

	public void setHash(Infohash hash) {
		checkMutable();
		this.hash = hash;
	}

	public String getPasskey() {
		return passkey;
	}

	public void setPasskey(String passkey) {
		checkMutable();
		this.passkey = passkey;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		checkMutable();
		this.peerId = peerId;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		checkMutable();
		this.event = event;
	}

	public long getUpDelta() {
		return upDelta;
	}

	public void setUpDelta(long upDelta) {
		checkMutable();
		this.upDelta = upDelta;
	}

	public long getDownDelta() {
		return downDelta;
	}

	public void setDownDelta(long downDelta) {
		checkMutable();
		this.downDelta = downDelta;
	}

	public long getTimeDelta() {
		return timeDelta;
	}

	public void setTimeDelta(int timeDelta) {
		checkMutable();
		this.timeDelta = timeDelta;
	}

	public long getLeft() {
		return left;
	}

	public void setLeft(long left) {
		checkMutable();
		this.left = left;
	}

	public int getNumwant() {
		checkMutable();
		return numwant;
	}

	public void setNumwant(int numwant) {
		checkMutable();
		this.numwant = numwant;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		checkMutable();
		this.address = address;
	}
	
	/**
	 * This prevents all further modifications to the object, making it immutable and thread safe. This
	 * should be called before it reaches the asynchronous queue. Any further modifications will throw
	 * an IllegalStateException.
	 */
	public void unmutable() {
		mutable = false;
	}
	
	private void checkMutable() {
		if(!mutable) {
			throw new IllegalStateException("Announce object finalized, unable to modify.");
		}
	}
}