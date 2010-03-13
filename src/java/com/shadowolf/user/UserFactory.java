package com.shadowolf.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.shadowolf.tracker.AnnounceException;

final public class UserFactory {
	private static ConcurrentHashMap<String, HashMap<String, User>> users = new ConcurrentHashMap<String, HashMap<String,User>>(1024); 

	private UserFactory() {}
	
	public static User getUser(final String peerId, final String passkey) throws AnnounceException {
		if(users.get(passkey) == null) {
			users.put(passkey, new HashMap<String, User>(1));
		}
		
		if (users.get(passkey).size() >= 3) {
			throw new AnnounceException("You can only be active from 3 locations at once!");
		} else 	{
			synchronized (users.get(passkey)) {
				if (users.get(passkey).get(peerId) == null){
					users.get(passkey).put(peerId, new User(peerId, passkey));;
				}
			}
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
					HashMap<byte[], Peer> peers = u.getPeers();
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
