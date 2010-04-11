package com.shadowolf.user;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.log4j.Logger;

import com.shadowolf.config.Config;


public class PeerList {
	private static final boolean DEBUG = false;
	private static final Logger LOGGER = Logger.getLogger(PeerList.class);

	final private ConcurrentSkipListMap<Long, Peer> seeds = new ConcurrentSkipListMap<Long, Peer>(); //NOPMD
	final private ConcurrentSkipListMap<Long, Peer> leechers = new ConcurrentSkipListMap<Long, Peer>(); //NOPMD

	public void doCleanUp() {
		this.cleanUpSeeds();
		this.cleanUpLeechers();
	}

	private void cleanUpLeechers() {
		if(DEBUG) {
			LOGGER.debug("Cleaning up leechers...");
			LOGGER.debug("Old count: " + this.leechers.size());
		}

		this.leechers.headMap(new Date().getTime() - Integer.parseInt(Config.getParameter("peer.timeout")) * 1000).clear();

		if(DEBUG) {
			LOGGER.debug("New count: " + this.leechers.size());
		}
	}

	private void cleanUpSeeds() {
		if(DEBUG) {
			LOGGER.debug("Cleaning up seeds...");
			LOGGER.debug("Old count: " + this.seeds.size());
		}

		this.seeds.headMap(new Date().getTime() - Integer.parseInt(Config.getParameter("peer.timeout")) * 1000).clear();

		if(DEBUG) {
			LOGGER.debug("New count: " + this.seeds.size());
		}
	}

	public void addSeeder(final Peer peer) {
		if(this.seeds.containsValue(peer)) {
			this.seeds.remove(peer.getLastAnnounce());
		}

		peer.setLastAnnounce(new Date());
		this.seeds.put(peer.getLastAnnounce(), peer);
	}

	public void addLeecher(final Peer peer) {
		if(this.leechers.containsValue(peer)) {
			this.leechers.remove(peer.getLastAnnounce());
		}

		peer.setLastAnnounce(new Date());
		this.leechers.put(peer.getLastAnnounce(), peer);
	}

	public void removeSeeder(final Peer peer) {
		this.seeds.remove(peer.getLastAnnounce());
	}

	public void removeLeecher(final Peer peer) {
		this.leechers.remove(peer.getLastAnnounce());
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
	public Peer[] getPeers(final int numwant) {
		if((this.leechers.size() == 0) && (this.seeds.size() == 0)) {
			return new Peer[0]; //NOPMD ... exit shortcut
		}

		final HashSet<Peer> peers = new HashSet<Peer>();

		final Iterator<Peer> leechVals = this.leechers.descendingMap().values().iterator();

		for(int i = 0; (i < numwant) && leechVals.hasNext(); i++) {
			peers.add(leechVals.next());

			if(peers.size() == numwant) {
				return peers.toArray(new Peer[peers.size()]); //NOPMD
			}
		}

		final Iterator<Peer> seedVals = this.seeds.descendingMap().values().iterator();

		for(int i = peers.size(); (i < numwant) && seedVals.hasNext(); i++) {
			peers.add(seedVals.next());

			if(peers.size() == numwant) {
				return peers.toArray(new Peer[peers.size()]); //NOPMD
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