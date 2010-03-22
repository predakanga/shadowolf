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
	private static final int USER_TIMEOUT = 7200; //two hours
	private static final boolean DEBUG = true;
	private static final Logger LOGGER = Logger.getLogger(UserFactory.class);
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, WeakReference<User>>> users =
				new ConcurrentHashMap<String, ConcurrentHashMap<String,WeakReference<User>>>(1024); 
	private static ConcurrentSkipListMap<Long, User> updates = new ConcurrentSkipListMap<Long,User>();
	
	public static final int MAX_LOCATIONS = 3;
	
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
			final User user = new User(peerId, passkey);
			users.get(passkey).put(peerId, new WeakReference<User>(user));
			updates.put(user.getLastAccessed(), user);
			
			return user; //NOPMD
		} else if (users.get(passkey).size() >= MAX_LOCATIONS) {
			throw new AnnounceException(TrackerResponse.Errors.TOO_MANY_LOCATIONS.toString());
		} 	
		
		User userRef = users.get(passkey).get(peerId).get();
		
		if(userRef == null) {
			if(DEBUG) {
				LOGGER.debug("Found cleaned up user!");
			}
			
			userRef = new User(peerId, passkey);
			users.get(passkey).put(peerId, new WeakReference<User>(userRef));
			updates.put(userRef.getLastAccessed(), userRef);
		} else {
			updates.remove(userRef.getLastAccessed());
			userRef.setLastAccessed(new Date().getTime());
			updates.put(userRef.getLastAccessed(), userRef);
		}
		
		return userRef;
	}
	
	public static User aggregate(final String passkey) {
		final UserAggregate user = new UserAggregate(passkey);
		
		if(users.get(passkey) == null) {
			LOGGER.debug("Returning default UA instance.");
			return user; //NOPMD
		} else if (users.get(passkey).size() == 1) {
			final User userRef = users.get(passkey).values().iterator().next().get();
			if(userRef == null) {
				users.put(passkey, new ConcurrentHashMap<String, WeakReference<User>>());
				LOGGER.debug("Returning default UA instance because of null WR instance.");
				return user; //NOPMD
			} else {
				if(DEBUG) {
					LOGGER.debug("Only found one user instance, directly returning it.");
				}
				
				return userRef;
			}
		} else {
			//this isn't necessary but fucked if I'm typing that more than once
			final ConcurrentHashMap<String, WeakReference<User>> set = users.get(passkey); 
			LOGGER.debug("Found " + set.size() + " user instances for passkey: " + passkey);
			
			final Iterator<String> iter = set.keySet().iterator();
			
			while(iter.hasNext()) {
				final 	String nextKey = iter.next();
				final User userRef = set.get(nextKey).get();
				if(userRef == null) {
					set.remove(nextKey);
				} else {
					
					final ConcurrentHashMap<String, WeakReference<Peer>> peers = userRef.getPeers();

					user.addPeerlist(peers);
					user.addDownloaded(userRef.getDownloaded());
					user.addUploaded(userRef.getUploaded());
					
					userRef.resetStats();
					
					updates.remove(userRef.getLastAccessed());
					userRef.setLastAccessed(new Date().getTime());//NOPMD ... this won't work unless we constantly poll for milliseconds
					updates.put(userRef.getLastAccessed(), userRef);
				}
			}
		}
		
		return user;
	}
}
