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
}
