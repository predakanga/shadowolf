package com.shadowolf.protocol;

import java.net.InetSocketAddress;

/**
 * This represents the actual data of an announce. It stores the Infohash,
 * passkey, peerId, event, the number of peers desired, as well as changes 
 * in upload, download, and time since the last announce.
 */
public interface Announce {
	public static enum Event {
		STARTED,
		STOPPED,
		ANNOUNCE,
		COMPLETE
	};
	
	public Infohash getHash();

	public String getPasskey();

	public String getPeerId();

	public Event getEvent();

	public long getUpDelta();

	public long getDownDelta();

	public long getTimeDelta();

	public long getLeft();

	public int getNumwant();
	
	public InetSocketAddress getAddress();
}