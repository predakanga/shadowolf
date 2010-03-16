package com.shadowolf.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerResponse;

final public class UserFactory {
	private static final Logger LOGGER = Logger.getLogger(UserFactory.class);
	private static ConcurrentHashMap<String, HashMap<String, User>> users = new ConcurrentHashMap<String, HashMap<String,User>>(1024); 
	public static final short MAX_LOCATIONS = 3;
	
	private UserFactory() {}
	
	public static User getUser(final String peerId, final String passkey) throws AnnounceException {
		if(users.get(passkey) == null) {
			LOGGER.debug("No entries for passkey: " + passkey);
			users.put(passkey, new HashMap<String, User>(1));
		}
		
		if (users.get(passkey).size() >= MAX_LOCATIONS) {
			throw new AnnounceException(TrackerResponse.Errors.TOO_MANY_LOCATIONS.toString());
		} else 	{
			synchronized (users.get(passkey)) {
				if (users.get(passkey).get(peerId) == null) {
					LOGGER.debug("Creating new User");
					users.get(passkey).put(peerId, new User(peerId, passkey));;
				}
			}
		}	
		
		return users.get(passkey).get(peerId);
	}
	
	public static User aggregate(String passkey) {
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
					HashMap<String, Peer> peers = u.getPeers();
					synchronized(peers) {
						user.addPeerlist(u.peers);
					}
					user.addDownloaded(u.getDownloaded());
					user.addUploaded(u.getUploaded());
					u.resetStats();
				}
			}
		}
		
		return user;
	}
}
