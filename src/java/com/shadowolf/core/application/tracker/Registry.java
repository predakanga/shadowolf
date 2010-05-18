package com.shadowolf.core.application.tracker;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javolution.util.FastMap;
import javolution.util.FastSet;

import org.apache.log4j.Logger;

/**
 * This class serves as a registry for all things torrent.
 * 
 */
public class Registry {
	private static boolean DEBUG = true;
	private static Logger LOGGER = Logger.getLogger(Registry.class);

	private static SortedSet<Client> clientList = new TreeSet<Client>();

	private static Map<ClientIdentifier, WeakReference<Client>> clients =
			new FastMap<ClientIdentifier, WeakReference<Client>>();

	private static Map<Integer, Set<ClientIdentifier>> torrents =
			new FastMap<Integer, Set<ClientIdentifier>>();

	private final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final static ReadLock readLock = lock.readLock();
	private final static WriteLock writeLock = lock.writeLock();

	public static void cleanup() {
		try {
			if(DEBUG) {
				LOGGER.debug("Starting cleanup, current client list size: " + clientList.size());
			}
			
			writeLock.lock();
			clientList.headSet(new HeadClient()).clear();
			
			if (DEBUG) {
				LOGGER.debug("Finished cleanup, current client list (hard references) size: " + clientList.size());
				LOGGER.debug("Starting cleanup, current clients (weak references) size: " + clients.size());
			}
			
			Iterator<ClientIdentifier> iter = clients.keySet().iterator();
			while(iter.hasNext()) {
				ClientIdentifier cid = iter.next();
				if(clients.get(cid) == null || clients.get(cid).get() == null) {
					iter.remove();
				}
			}
			
			if(DEBUG){ 
				LOGGER.debug("Finished cleanup, current clients (weak references) size: " + clients.size());
				LOGGER.debug("Starting cleanup, current torrent list size: " + torrents.size());
			}
			
			Iterator<Integer> torrentIter = torrents.keySet().iterator();
			
			while(torrentIter.hasNext()) {
				Integer torrentId = torrentIter.next();
				if(torrents.get(torrentId) == null || torrents.get(torrentId).size() == 0) {
					torrentIter.remove();
				} else {
					Set<ClientIdentifier> set = torrents.get(torrentId);
					
					Iterator<ClientIdentifier> setIter = set.iterator();
					
					while(setIter.hasNext()) {
						ClientIdentifier cid = setIter.next();
						if(clients.get(cid) == null || clients.get(cid).get() == null) {
							setIter.remove();
						}
					}
					
					if(set.size() == 0) {
						torrentIter.remove();
					}
				}
			}
			
			if (DEBUG) {
				LOGGER.debug("Finished cleanup, current torrent list size: " + torrents.size());
			}
		} finally {
			writeLock.unlock();
		}

	}

	public static int clients() {
		try {
			readLock.lock();
			return clientList.size();
		} finally {
			readLock.unlock();
		}
	}

	public static int torrents() {
		try {
			readLock.lock();
			return torrents.size();
		} finally {
			readLock.unlock();
		}
	}

	public static boolean containsClient(ClientIdentifier identifier) {
		try {
			readLock.lock();
			return clients.containsKey(identifier);
		} finally {
			readLock.unlock();
		}
	}

	public static boolean containsTorrent(Integer torrentId) {
		try {
			readLock.lock();
			return torrents.containsKey(torrentId);
		} finally {
			readLock.unlock();
		}
	}

	public static Client getClient(ClientIdentifier identifier) {
		try {
			readLock.lock();
			WeakReference<Client> clientReference = clients.get(identifier);
			Client client;
			
			if(clientReference == null) {
				try { 
					readLock.unlock();
					writeLock.lock();
					client = new Client(identifier);
					clientReference = new WeakReference<Client>(client);
				} finally {
					readLock.lock();
					writeLock.unlock();
				}
				
			} else {
				client = clientReference.get();
				
				try { 
					readLock.unlock();
					writeLock.lock();
					if(client == null) {
						client = new Client(identifier);
						clientReference = new WeakReference<Client>(client);
					}
				} finally {
					readLock.lock();
					writeLock.unlock();
				}
			}
			
			client.setLatestAccess();
			return client;
		} finally {
			readLock.unlock();
		}
	}

	public static Set<Client> getClientSet() {
		try {
			readLock.lock();
			return Collections.unmodifiableSet(clientList);
		} finally {
			readLock.unlock();
		}
	}
	
	public static Set<ClientIdentifier> getClientList(Integer torrentId) {
		ensureTorrentSet(torrentId);
		try {
			readLock.lock();
			return Collections.unmodifiableSet(torrents.get(torrentId));
		} finally {
			readLock.unlock();
		}
		
	}

	private static void ensureTorrentSet(Integer id) {
		try {
			readLock.lock();
			if (torrents.get(id) == null) {
				
				torrents.put(id, new FastSet<ClientIdentifier>());
			}
		} finally {
			readLock.unlock();
		}
	}

}
