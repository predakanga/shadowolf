package com.shadowolf.user;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.log4j.Logger;

import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerResponse;

final public class UserFactory {
	private static final int USER_TIMEOUT = 30;
	private static final boolean DEBUG = true;
	private static final Logger LOGGER = Logger.getLogger(UserFactory.class);
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, WeakReference<User>>> users =
				new ConcurrentHashMap<String, ConcurrentHashMap<String,WeakReference<User>>>(1024); 
	private static ConcurrentSkipListMap<Long, User> updates = new ConcurrentSkipListMap<Long,User>();
	
	public static final short MAX_LOCATIONS = 3;
	
	private UserFactory() {}
	
	public static void cleanUp() {
		if(DEBUG) {
			LOGGER.debug("Cleaning up users...");
			LOGGER.debug("Old count: " + updates.size());
		}
		
		updates.headMap(new Date().getTime() - USER_TIMEOUT * 1000).clear();
	
		if(DEBUG) {
			LOGGER.debug("New count: " + updates.size());
		}
	}
	
	public static User getUser(final String peerId, final String passkey) throws AnnounceException {
		if(users.get(passkey) == null) {
			users.put(passkey, new ConcurrentHashMap<String, WeakReference<User>>(1));
		}
		
		if (users.get(passkey).get(peerId) == null) {
			User u = new User(peerId, passkey);
			users.get(passkey).put(peerId, new WeakReference<User>(u));
			updates.put(u.getLastAccessed(), u);
			
			return u;
		} else if (users.get(passkey).size() >= MAX_LOCATIONS) {
			throw new AnnounceException(TrackerResponse.Errors.TOO_MANY_LOCATIONS.toString());
		} 	
		
		User u = users.get(passkey).get(peerId).get();
		
		if(u != null) {
			updates.remove(u.getLastAccessed());
			u.setLastAccessed(new Date().getTime());
			updates.put(u.getLastAccessed(), u);
		} else {
			if(DEBUG) {
				LOGGER.debug("Found cleaned up user!");
			}
			
			u = new User(peerId, passkey);
			users.get(passkey).put(peerId, new WeakReference<User>(u));
			updates.put(u.getLastAccessed(), u);
		}
		
		return u;
	}
	
	public static User aggregate(String passkey) {
		UserAggregate user = new UserAggregate(passkey);
		
		if(users.get(passkey) == null) {
			return user;
		} else if (users.get(passkey).size() == 1) {
			User u = users.get(passkey).values().iterator().next().get();
			if(u == null) {
				users.put(passkey, new ConcurrentHashMap<String, WeakReference<User>>());
				return user;
			}
		} else {
			//this isn't necessary but fucked if I'm typing that more than once
			ConcurrentHashMap<String, WeakReference<User>> set = users.get(passkey); 
			Iterator<String> iter = set.keySet().iterator();
			
			while(iter.hasNext()) {
				String nextKey = iter.next();
				User u = set.get(nextKey).get();
				if(u == null) {
					set.remove(nextKey);
				} else {
					
					ConcurrentHashMap<String, WeakReference<Peer>> peers = u.getPeers();

					user.addPeerlist(peers);
					user.addDownloaded(u.getDownloaded());
					user.addUploaded(u.getUploaded());
					
					u.resetStats();
					
					updates.remove(u.getLastAccessed());
					u.setLastAccessed(new Date().getTime());
					updates.put(u.getLastAccessed(), u);
				}
			}
		}
		
		return user;
	}
}
