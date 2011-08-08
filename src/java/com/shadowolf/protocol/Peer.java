package com.shadowolf.protocol;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.ThreadSafe;

/**
 * 
 * This represents the actual peer of a torrent. It stores information about
 * their current activity, primarily their InetAddress, and the upload and
 * download amounts. These amounts are on a per session basis, and accumulate
 * over time.
 *
 */
@ThreadSafe
public class Peer {
	private final InetSocketAddress address;
	
	private final AtomicLong lastUpdate = new AtomicLong(0);
	private final AtomicLong uploaded = new AtomicLong(0);
	private final AtomicLong downloaded = new AtomicLong(0);
	
	
	Peer(InetSocketAddress address) {
		this.address = address;
		lastUpdate.set(System.currentTimeMillis());
	}
	
	
	public void touch() {
		lastUpdate.set(System.currentTimeMillis());
	}
	
	public long addDownload(long toAdd) {
		return downloaded.addAndGet(toAdd);
	}
	
	public long addUpload(long toAdd) {
		return uploaded.addAndGet(toAdd);
	}
	
	public InetSocketAddress getAddress() {
		return address;
	}

	public long getLastUpdate() {
		return lastUpdate.get();
	}

	public long getUploaded() {
		return uploaded.get();
	}

	public void setUploaded(long uploaded) {
		this.uploaded.set(uploaded);
	}

	public long getDownloaded() {
		return downloaded.get();
	}

	public void setDownloaded(long downloaded) {
		this.downloaded.set(downloaded);
	}
}
