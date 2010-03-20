package com.shadowolf.user;

import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;


public class UserAggregate extends User {

	public UserAggregate(final String passkey) {
		super(null, passkey);
	}

	public void addPeerlist(final ConcurrentHashMap<String, WeakReference<Peer>> list) {
		this.peers.putAll(list);
	}
	
	@Override
	public void updateStats(final String infoHash, final long uploaded, final long downloaded, 
			final String ipAddress, final String port) throws IllegalAccessException, UnknownHostException {
		
		throw new IllegalAccessException("Cannot update stats from a UserAggregate instance");
	}
	
	@Override
	public Peer getPeer(final String infoHash, final String ipAddress, final String port) 
			throws IllegalAccessException, UnknownHostException {
		
		throw new IllegalAccessException("Cannot get peer from UserAggregate instance");
	}
}
