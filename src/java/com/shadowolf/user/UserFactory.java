package com.shadowolf.user;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.Peer;

final public class UserFactory {
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, User>> users = new ConcurrentHashMap<String, ConcurrentHashMap<String,User>>(1024, 0.66F, 24); 

	private UserFactory() {}
	
	public static User getUser(final String peerId, final String passkey) throws AnnounceException {
		if(users.get(passkey) == null) {
			users.put(passkey, new ConcurrentHashMap<String, User>(3, 1.0F, 4));
		}
		
		if (users.get(passkey).size() >= 3) {
			throw new AnnounceException("You can only be active from 3 locations at once!");
		} else if (users.get(passkey).get(peerId) == null){
			users.get(passkey).put(peerId, new User(peerId, passkey));
		}	
		
		return users.get(passkey).get(peerId);
	}
	
	public User aggregate(String passkey) {
		UserAggregate user = new UserAggregate(passkey);
		
		if(users.get(passkey) == null) {
			return user;
		} else if (users.get(passkey).size() == 1) {
			return users.get(passkey).values().iterator().next();
		} else {
			synchronized(users.get(passkey)) {
				Iterator<User> iter = users.get(passkey).values().iterator();
				
				while(iter.hasNext()) {
					final User u = iter.next();
					ConcurrentHashMap<Long, Peer> peers = u.getPeers();
					synchronized(peers) {
						user.addPeerlist(u.peers);
					}
					user.addDownloaded(u.getDownloaded());
					user.addUploaded(u.getUploaded());
				}
			}
		}
		
		return user;
	}
}
