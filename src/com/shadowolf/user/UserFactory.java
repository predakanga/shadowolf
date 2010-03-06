package com.shadowolf.user;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

final public class UserFactory {
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, User>> users = new ConcurrentHashMap<String, ConcurrentHashMap<String,User>>(1024, 0.66F, 24); 

	private UserFactory() {
		
	}
	
	public static User getUser(final String peerId, final String passkey) {
		User u;
		if(users.containsKey(passkey)) {
			if((u = users.get(passkey).get(peerId)) != null) {
				return u;
			} else {
				u = new User(peerId, passkey);
				return users.get(passkey).put(peerId, u);
			}
		} else {
			u = new User(peerId, passkey);
			users.put(passkey, new ConcurrentHashMap<String, User>(3, 1.0F, 4));
			return users.get(passkey).put(peerId, u);
		}
		
	}
	
	public synchronized UserAggregate aggregate(String passkey) {
		Iterator<User> iter = users.get(passkey).values().iterator();
		
		UserAggregate user = new UserAggregate(passkey);
		
		while(iter.hasNext()) {
			final User u = iter.next();
			user.addPeerlist(u.getPeers());
		}
		
		return user;
	}
}
