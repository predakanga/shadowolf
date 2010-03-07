package com.shadowolf.user;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.Peer;

final public class UserFactory {
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, User>> users = new ConcurrentHashMap<String, ConcurrentHashMap<String,User>>(1024, 0.66F, 24); 

	private UserFactory() {}
	
	public static User getUser(final String peerId, final String passkey) throws AnnounceException {
		User u;
		if(users.containsKey(passkey)) {
			if((u = users.get(passkey).get(peerId)) != null) {
				return u;
			} else if (users.get(passkey).size() >= 3) {
				throw new AnnounceException("You can only be active from 3 locations at once!");
			} else {
				u = new User(peerId, passkey);
				return users.get(passkey).put(peerId, u);
			}
		}  else {
			u = new User(peerId, passkey);
			users.put(passkey, new ConcurrentHashMap<String, User>(3, 1.0F, 4));
			return users.get(passkey).put(peerId, u);
		}
		
	}
	
	public UserAggregate aggregate(String passkey) {
		UserAggregate user = new UserAggregate(passkey);
		
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
			
			return user;
		}
	}
}
