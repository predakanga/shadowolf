package com.shadowolf.user;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.log4j.Logger;


public class PeerList {
	private static final int PEER_TIMEOUT = 30;
	private static final boolean DEBUG = true;
	private static final Logger LOGGER = Logger.getLogger(PeerList.class);
	
	private ConcurrentSkipListMap<Long, Peer> seeds = new ConcurrentSkipListMap<Long, Peer>();
	private ConcurrentSkipListMap<Long, Peer> leechers = new ConcurrentSkipListMap<Long, Peer>();
	
	public void doCleanUp() {
		this.cleanUpSeeds();
		this.cleanUpLeechers();
	}
	
	private void cleanUpLeechers() {
		if(DEBUG) {
			LOGGER.debug("Cleaning up leechers...");
			LOGGER.debug("Old count: " + this.leechers.size());
		}
		
		this.leechers.headMap(new Date().getTime() - PEER_TIMEOUT * 1000).clear();
		
		if(DEBUG) {
			LOGGER.debug("New count: " + this.leechers.size());
		}
	}
	
	private void cleanUpSeeds() {
		if(DEBUG) {
			LOGGER.debug("Cleaning up seeds...");
			LOGGER.debug("Old count: " + this.seeds.size());
		}
		
		this.seeds.headMap(new Date().getTime() - PEER_TIMEOUT * 1000).clear();
		
		if(DEBUG) {
			LOGGER.debug("New count: " + this.seeds.size());
		}
	}
	
	public void addSeeder(Peer p) {
		if(this.seeds.containsValue(p)) {
			this.seeds.remove(p.getLastAnnounce());
		}

		p.setLastAnnounce(new Date());
		this.seeds.put(p.getLastAnnounce(), p);
	}
	
	public void addLeecher(Peer p) {
		if(this.leechers.containsValue(p)) {
			this.leechers.remove(p.getLastAnnounce());
		}
		
		p.setLastAnnounce(new Date());
		this.leechers.put(p.getLastAnnounce(), p);
	}
	
	public void removeSeeder(Peer p) {
		this.seeds.remove(p.getLastAnnounce());
	}
	
	public void removeLeecher(Peer p) {
		this.leechers.remove(p.getLastAnnounce());
	}
	
	/**
	 * Returns an array of peers with the given size.  Since we store the peers (both leechers and seeders)
	 * internally as a sorted map based on their announce time, the ordering of the peers is constantly changing.
	 * Because of this, and Java's limitations on sane ways to randomly retrieve values from ConcurrentSkipListMap,
	 * we just return the first numwant leechers if there are enough, otherwise, we start pushing seeders onto the
	 * returned array until it's large enough.
	 * 
	 * @param numwant the number of peers (size of the returned array) to return.
	 * @return Peer[] the array of peers
	 */
	public Peer[] getPeers(int numwant) {
		if(this.leechers.size() == 0 && this.seeds.size() == 0) {
			return new Peer[0];
		}

		HashSet<Peer> peers = new HashSet<Peer>();
		
		Iterator<Peer> leechVals = this.leechers.descendingMap().values().iterator();
		
		for(int i = 0; i < numwant && leechVals.hasNext(); i++) {
			peers.add(leechVals.next());
			
			if(peers.size() == numwant) {
				return peers.toArray(new Peer[peers.size()]);
			}
		}
		
		Iterator<Peer> seedVals = this.seeds.descendingMap().values().iterator();
		
		for(int i = peers.size(); i < numwant && seedVals.hasNext(); i++) {
			peers.add(seedVals.next());
			
			if(peers.size() == numwant) {
				return peers.toArray(new Peer[peers.size()]);
			}
		}
		
		return peers.toArray(new Peer[peers.size()]);
	}

	public int getSeederCount() {
		return this.seeds.size();
	}	
	
	public int getLeecherCount() {
		return this.leechers.size();
	}
}