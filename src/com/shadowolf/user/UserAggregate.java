package com.shadowolf.user;

import java.util.concurrent.ConcurrentHashMap;

import com.shadowolf.tracker.Peer;

public class UserAggregate extends User {

	public UserAggregate(String passkey) {
		super();
		
		this.passkey = passkey;
	}

	public void addPeerlist(ConcurrentHashMap<Long, Peer> list) {
		this.peers.putAll(list);
	}
	
	@Override
	public void updateStats(long infoHash, long uploaded, long downloaded) throws IllegalAccessException {
		throw new IllegalAccessException("Cannot update stats from a UserAggregate instance");
	}
	
	@Override
	public Peer getPeer(final long infoHash) throws IllegalAccessException {
		throw new IllegalAccessException("Cannot get peer from UserAggregate instance");
	}
}
