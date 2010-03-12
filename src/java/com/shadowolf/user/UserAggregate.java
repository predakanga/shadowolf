package com.shadowolf.user;

import java.net.UnknownHostException;
import java.util.HashMap;

import com.shadowolf.tracker.Peer;

public class UserAggregate extends User {

	public UserAggregate(String passkey) {
		super();
		
		this.passkey = passkey;
	}

	public void addPeerlist(HashMap<byte[], Peer> list) {
		this.peers.putAll(list);
	}
	
	@Override
	public void updateStats(byte[] infoHash, long uploaded, long downloaded, String ipAddress, String port) throws IllegalAccessException, UnknownHostException {
		throw new IllegalAccessException("Cannot update stats from a UserAggregate instance");
	}
	
	@Override
	public Peer getPeer(final byte[] infoHash, String ipAddress, String port) throws IllegalAccessException, UnknownHostException {
		throw new IllegalAccessException("Cannot get peer from UserAggregate instance");
	}
}
