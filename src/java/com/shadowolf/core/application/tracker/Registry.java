package com.shadowolf.core.application.tracker;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import com.shadowolf.core.application.cache.InfoHashCache;
import com.shadowolf.util.ReservoirSampler;

/**
 * This class serves as a registry for all things torrent.
 * <br/><br/>
 * This should be the central access point for all access to the core tracker itself.  
 * The actual tracker logic is encapsulated within this class (and the rest of the package).  
 */
public class Registry {
	private static boolean DEBUG = true;
	private static Logger LOGGER = Logger.getLogger(Registry.class);

	/**
	 * The clientList is a SortedSet of all clients.  It's sorted by Client last modification 
	 * so whenever anything is done to a client it needs to be pulled out of this collection
	 * and pushed back into it.  The sorting is done so that garbage Clients that don't
	 * clean themselves up (which will happen for various reasons) can be periodically cleaned out.
	 */
	private static SortedSet<Client> clientList = new TreeSet<Client>(new Comparator<Client>() {
		@Override
		public int compare(Client arg0, Client arg1) {
			if(arg0.getLatestAccess() > arg0.getLatestAccess()) {
				return 1;
			} else if (arg0.getLatestAccess() < arg1.getLatestAccess()) {
				return -1;
			} else {
				return 0;
			}
		}
		
	});

	/**
	 * <i>clients</i> is a Map of ClientIdentifier -> WeakReference&lt;Client&gt; that serves
	 * as a lookup table for clients based on their identifier.  The references are weak so
	 * that the strong references can be stored in <i>clientList</i> and cleaned up accordingly.
	 */
	private static Map<ClientIdentifier, WeakReference<Client>> clients =
			new FastMap<ClientIdentifier, WeakReference<Client>>();

	/**
	 * <i>seeders and leechers</i> are both a Map of Integer -> Set&lt;ClientIdentifier&gt; that serve as a lookup
	 * table for torrents based on their id.  The value is a set of ClientIdentifiers that contain
	 * peers for this torrent.  Rather than keep references to the Client instances themselves (which
	 * can be easily obtained by accessing {@link #getClient(ClientIdentifier)}) or Peer instances,
	 * we use ClientIdentifiers because they contain all the pertinent information that the tracker
	 * needs to send out on an announce or scrape response (IP and port) and allows for easy computation
	 * of the size of a particular torrent's swarm.
	 */
	private static Map<Integer, Set<ClientIdentifier>> seeders =
			new FastMap<Integer, Set<ClientIdentifier>>();

	private static Map<Integer, Set<ClientIdentifier>> leechers =
		new FastMap<Integer, Set<ClientIdentifier>>();
	
	/**
	 * This lock (well, its children) guard this entire collection.  Because the three collections
	 * that make up the state of this class <strong>absolutely have to be synchronized with one another
	 * at all times</strong>, the same locks are used for any modification.
	 */
	private final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final static ReadLock readLock = lock.readLock();
	private final static WriteLock writeLock = lock.writeLock();

	/**
	 * Adds a {@link Client} to the tracker on the specified <i>torrentId</i>'s swarm.  We pass in a Client
	 * here, as it's necessary to ensure that the Client itself is added to the registry and sorted, but also
	 * the Client instance exposes its clientIdentifier, which is what we need for the swarm tracking. This method
	 * is package private, as the Client instance should call this method on itself, and shouldn't be called publically.
	 * @param client the Client instance to add to a particular swam
	 * @param torrentId the ID of the torrent to add to the swarm
	 * @see Client#getClientId()
	 * @see #getClient(ClientIdentifier)}
	 * @see Announce#getClientIdentifier()}
	 * @see InfoHashCache#lookupTorrentId(String)}
	 * @see {@link Client#getPeer(Integer)}
	 */
	static void addPeer(Client client, Integer torrentId, boolean isSeeder) {
		try {
			writeLock.lock();
			//put a client reference in the clientidentifier lookup table
			clients.put(client.getClientId(), new WeakReference<Client>(client));
			
			//add the client id to the torrent lookup table
			
			if(isSeeder) {
				ensureSeederSet(torrentId);
				seeders.get(torrentId).add(client.getClientId());
			} else {
				ensureLeecherSet(torrentId);
				leechers.get(torrentId).add(client.getClientId());
			}
			
			//and add the client to the sorted client list
			client.setLatestAccess();
			clientList.remove(client); //we remove and re-add, if it exists, for sorting purposes
			clientList.add(client);
		} finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * Removes a Client from all swarms specified by the Iterable "list" of torrent IDs.  This method
	 * is package private because it is called by the Client itself, with its internal list of torrents.
	 * Rather than expose a method that removed a Client simply with a formal parameter list of just the Client
	 * itself, we use this methodology so that we don't have to maintain a fourth collection (a map of 
	 * ClientIdentifier -&gt; torrentIds) or iterate through the torrents collection, and iterate through all of
	 * its sets.  We need the Client as a parameter so the Client can be removed from the client collections as well. 
	 * @param client the Client instance to remove from this tracker
	 * @param torrentIds the "list" of all torrent Ids to remove it from
	 * @see {@link Client#destroy()}
	 * @see {@link #seeders}
	 * @see {@link #clientList}
	 * @see {@link #clients}
	 */
	static void removeClient(Client client, Iterable<Integer> torrentIds) {
		try {
			writeLock.lock();
			
			//remove the reference from the client id lookup table
			clients.remove(client.getClientId());
			
			//remove all torrents
			for(Integer i : torrentIds) {
				Set<ClientIdentifier> set = seeders.get(i);
				if(set != null) { 
					set.remove(client.getClientId());
				}
			}
			
			//and remove the client from the sorted client list
			client.setLatestAccess();
			clientList.remove(client); 
		} finally {
			writeLock.unlock();
		}
	}
	
	
	/**
	 * Removes a ClientIdentifier from a particular client.  We only need the client identifier here
	 * because we presume that the client has other torrents; if it does not, the client takes care of that 
	 * itself.  Like {@link #removeClient(Client, Iterable)} and {@link #addPeer(Client, Integer)}, this
	 * method is package private because Client instances call it themselves and this functionality
	 * does not need to be publicly accessable.
	 * @param identifier the ClientIdentifier for the Client to be removed from a swarm.
	 * @param torrentId the torrent ID for the swarm to remove a Client from.
	 * @see {@link Client#removePeer(Integer)}
	 */
	static void removePeer(ClientIdentifier identifier, Integer torrentId) {
		try {
			writeLock.lock();
			
			Set<ClientIdentifier> set = seeders.get(torrentId);
			if(set != null) { 
				set.remove(identifier);
			}
			
			set = leechers.get(torrentId);
			if(set != null) { 
				set.remove(identifier);
			}
			
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Runs cleanup, theoretically cleaning all "garbage" out of this class.  
	 * 
	 * This is a multi step process:
	 * <ul>
	 * 	<li>
	 * 		Get the headSet() of the <i>clientList<i> set (set is ordered by access time) based on an instance of {@link HeadClient}.  
	 * 		HeadClient does nothing but return an access time of one hour - the threshold for access before a Client is considered garbage.
	 * 	</li>
	 * 
	 * 	<li>
	 * 		Iterate over the headSet obtained in the previous step, removing all the Client's identifiers from {@link #clientList} and 
	 * 		calling destroy() on that Client, theoretically clearing out all of its peers.
	 * 	</li>
	 * 
	 * 	<li>
	 * 		.clear() the headSet from the first step, removing it from the {@link clients} set.
	 * 	</li>
	 * 
	 * 	<li>
	 * 		Mostly as a paranoia measure, also iterate over the torrents lookup table, removing clients we've destroyed.
	 * 	</li>
	 * </ul>
	 * @param removeSeconds the age of Peers to remove, anything that hasn't been accessed in <i>removeSeconds</i> is removed. 
	 */
	public static void cleanup(long removeSeconds) {
		try {
			if(DEBUG) {
				LOGGER.debug("Starting cleanup, current client list size: " + clientList.size());
			}
			
			writeLock.lock();
			Set<Client> toRemove = clientList.headSet(new HeadClient());
			
			for(Client c : toRemove) {
				clientList.remove(c.getClientId());
				c.destroy();
			}
			toRemove.clear();
			
			if (DEBUG) {
				LOGGER.debug("Finished cleanup, current client list (hard references) size: " + clientList.size());
				LOGGER.debug("Starting cleanup, current torrent list size: " + seeders.size());
			}
			
			cleanTorrentCollection(seeders.keySet().iterator(), removeSeconds);
			cleanTorrentCollection(leechers.keySet().iterator(), removeSeconds);
			
			if (DEBUG) {
				LOGGER.debug("Finished cleanup, current torrent list size: " + seeders.size());
			}
		} finally {
			writeLock.unlock();
		}

	}

	/**
	 * Returns an iterable collection of ClientIdentifiers that's suited to feeding to a client.  Ordering of the CID's is not 
	 * guaranteed and somewhat random.
	 * @param torrentId the torrent ID from which to pull peers
	 * @param numwant the number of wanted peers
	 * @param preferLeechers whether to prefer leechers or not; seeders should anounce true, leechers false.
	 * @return
	 */
	public static Iterable<ClientIdentifier> getAnnounceablePeerlist(Integer torrentId, int numwant, boolean preferLeechers) {
		try {
			readLock.lock();
			Set<ClientIdentifier> peers; 
			
			if(preferLeechers) {
				//in this case, we have a seeder announcing, so fuck 'em if there aren't enough, they don't need other seeds
				ensureLeecherSet(torrentId);
				peers = leechers.get(torrentId);
				
				if(peers.size() > numwant) {
					return ReservoirSampler.listSample(peers, numwant);
				} else {
					return Collections.unmodifiableSet(peers);
				}
				
			} else {
				//this is a leecher, so they want mostly leechers but we'll give them seeds if there are too few (<50).
				ensureLeecherSet(torrentId);
				peers = leechers.get(torrentId);
				
				if(peers.size() > numwant) {
					return ReservoirSampler.listSample(peers, numwant);
				} else if (peers.size() >= 50) {
					return Collections.unmodifiableSet(peers);
				} else {
					ensureSeederSet(torrentId);
					Set<ClientIdentifier> list = new FastSet<ClientIdentifier>();
					list.addAll(peers);
					list.addAll(ReservoirSampler.listSample(seeders.get(torrentId), numwant - peers.size()));
					return Collections.unmodifiableSet(list);
				}
			}
			
		} finally {
			readLock.unlock();
		}
	}

	private static void cleanTorrentCollection(Iterator<Integer> torrentIter, long removeSeconds) {
		while(torrentIter.hasNext()) {
			Integer torrentId = torrentIter.next();
			if(seeders.get(torrentId) == null || seeders.get(torrentId).size() == 0) {
				torrentIter.remove();
			} else {
				Set<ClientIdentifier> set = seeders.get(torrentId);
				
				Iterator<ClientIdentifier> setIter = set.iterator();
				
				while(setIter.hasNext()) {
					ClientIdentifier cid = setIter.next();
					
					WeakReference<Client> client = clients.get(cid);
					
					if(client != null) {
						Client cli = client.get();
						if(cli == null) {
							clients.remove(client);
						} else {
							long removeTime = new Date().getTime() - removeSeconds * 1000;
							Map<Integer, Peer> peers = cli.getPeers();
							Iterator<Integer> iter = peers.keySet().iterator();
							for(Integer i : peers.keySet()) {
								if(peers.get(i).getLatestAnnounce() < removeTime) {
									iter.remove();
								}
							}
							
						}
					}
					
					if(clients.get(cid) == null || clients.get(cid).get() == null) {
						setIter.remove();
					}
				}
				
				if(set.size() == 0) {
					torrentIter.remove();
				}
			}
		}
	}

	/**
	 * Get the number of clients this tracker tracks.  This is *NOT* a peercount or total swarmcount,
	 * but rather a count of the total unique user-machine combination that the tracker is responsible for. 
	 * It's provided for planned statistical analysis.
	 * @return the number of unique user-machine combinations ("Clients").
	 */
	public static int clients() {
		try {
			readLock.lock();
			return clientList.size();
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Get the total number of torrents <i>currently tracker</i>.  For a normal tracker, this number will
	 * almost always be less than {@link InfoHashCache#size()} because not all torrents will have a swarm.
	 * <br/><br/>
	 * Active torrents can be calculated like so: {@link InfoHashCache#size()} - torrents().
	 * @return the total number of active torrents.
	 */
	public static int torrents() {
		try {
			readLock.lock();
			Set<Integer> tempSet = new FastSet<Integer>(seeders.size() + leechers.size());
			tempSet.addAll(seeders.keySet());
			tempSet.addAll(leechers.keySet());
			return tempSet.size();
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Get the total number of seeders currently tracked.
	 * @return the number of seeders currently tracked.
	 */
	public static int seeders() {
		return countTorrentCollection(seeders);
	}
	/**
	 * Get the total number of leechers.
	 * @return the number of leechers.
	 */
	public static int leechers() {
		return countTorrentCollection(leechers);
	}
	private static int countTorrentCollection(Map<Integer, Set<ClientIdentifier>> set) {
		try {
			readLock.lock();
			int count = 0;
			for(Set<ClientIdentifier> current : set.values()) {
				if(current != null) {
					count += current.size();
				}
			}
			return count;
		} finally {
			readLock.unlock();
		}
	}
	/**
	 * Determine whether or not a particular client, identified by <i>identifier</i> is active.
	 * @param identifier The client identifier for the clietn we'd like to check activity for
	 * @return whether the client is active or not
	 */
	public static boolean containsClient(ClientIdentifier identifier) {
		try {
			readLock.lock();
			return clients.containsKey(identifier);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Determine whether a torrent, identified by <i>torrentId</i> is being tracked.
	 * @param torrentId the torrent to check
	 * @return whether or not the torrent is actively tracked
	 */
	public static boolean containsTorrent(Integer torrentId) {
		try {
			readLock.lock();
			return seeders.containsKey(torrentId) || leechers.containsKey(torrentId);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Returns a Client instance based on the identifier.  This *WILL* create a Client instance
	 * if one does not exist.  <strong>THIS METHOD SHOULD BE THE ONLY WAY THAT CLIENT INSTANCES
	 * ARE EVER REFERENCED</strong>.
	 * @param identifier the ClientIdentifer that identifies the Client to retrieve.
	 * @return the Client instance identified by the supplied identifier.
	 */
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
			clientList.remove(client);
			clientList.add(client);
			return client;
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Returns a Set of ClientIdentifiers (for seeders) that make up the swarm for the supplied
	 * torrent ID.
	 */
	public static Set<ClientIdentifier> getSeederList(Integer torrentId) {
		ensureSeederSet(torrentId);
		try {
			readLock.lock();
			return Collections.unmodifiableSet(seeders.get(torrentId));
		} finally {
			readLock.unlock();
		}
		
	}

	/**
	 * Returns a Set of ClientIdentifiers (for leechers) that make up the swarm for the supplied
	 * torrent ID.
	 */
	public static Set<ClientIdentifier> getLeecherList(Integer torrentId) {
		ensureLeecherSet(torrentId);
		try {
			readLock.lock();
			return Collections.unmodifiableSet(leechers.get(torrentId));
		} finally {
			readLock.unlock();
		}
		
	}
	
	private static void ensureSeederSet(Integer id) {
		try {
			readLock.lock();
			if (seeders.get(id) == null) {
				seeders.put(id, new FastSet<ClientIdentifier>());
			}
		} finally {
			readLock.unlock();
		}
	}
	
	private static void ensureLeecherSet(Integer id) {
		try {
			readLock.lock();
			if (leechers.get(id) == null) {
				leechers.put(id, new FastSet<ClientIdentifier>());
			}
		} finally {
			readLock.unlock();
		}
	}

}
