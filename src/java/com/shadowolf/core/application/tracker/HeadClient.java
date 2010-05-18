package com.shadowolf.core.application.tracker;

import java.util.Date;
import java.util.Map;

class HeadClient extends Client {
	HeadClient() {
		super(null);
	}

	@Override
	public void addDownloaded(final long downloaded) {
		super.addDownloaded(downloaded);
	}

	@Override
	public void addUploaded(final long uploaded) {
		super.addUploaded(uploaded);
	}

	@Override
	public boolean equals(final Object obj) {
		throw new UnsupportedOperationException("This method not supported in HeadClient");
	}

	@Override
	public long getDownloaded() {
		throw new UnsupportedOperationException("This method not supported in HeadClient");
	}

	@Override
	public long getLatestAccess() {
		return new Date().getTime() - 3600000; //1 hour old
	}

	@Override
	public Peer getPeer(final Integer torrentId) {
		throw new UnsupportedOperationException("This method not supported in HeadClient");
	}

	@Override
	public Map<Integer, Peer> getPeers() {
		throw new UnsupportedOperationException("This method not supported in HeadClient");
	}

	@Override
	public long getUploaded() {
		throw new UnsupportedOperationException("This method not supported in HeadClient");
	}

	@Override
	public boolean hasTorrent(final Integer torrentId) {
		throw new UnsupportedOperationException("This method not supported in HeadClient");
	}

	@Override
	public void removePeer(final Integer torrentId) {
		throw new UnsupportedOperationException("This method not supported in HeadClient");
	}

	@Override
	public void removePeer(final Peer peer) {
		throw new UnsupportedOperationException("This method not supported in HeadClient");
	}

	@Override
	public void setLatestAccess() {
		throw new UnsupportedOperationException("This method not supported in HeadClient");
	}

	@Override
	public void updateStats(final Integer torrentId, final long uploaded, final long downloaded) {
		throw new UnsupportedOperationException("This method not supported in HeadClient");
	}

}
