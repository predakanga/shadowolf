package com.shadowolf.user;

import java.util.concurrent.ConcurrentHashMap;

import com.shadowolf.tracker.Peer;

public class UserAggregate extends User {
	public UserAggregate(){
		//overrides so as to not throw exceptions!
	}
	
	public void addPeerlist(ConcurrentHashMap<Long, Peer> list) {
		this.peers.putAll(list);
	}
	
	
}
