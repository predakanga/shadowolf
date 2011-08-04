package com.shadowolf.protocol;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.ThreadSafe;

/**
 * TBD
 *
 */
@ThreadSafe
public class Peer {
	private InetSocketAddress address;
	
	private AtomicLong lastUpdate = new AtomicLong(0);
	private AtomicLong uploaded = new AtomicLong(0);
	private AtomicLong downloaded = new AtomicLong(0);
	
	
	public Peer(InetSocketAddress address) {
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

	public void setAddress(InetSocketAddress address) {
		this.address = address;
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
