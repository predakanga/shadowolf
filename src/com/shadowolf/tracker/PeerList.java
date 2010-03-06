package com.shadowolf.tracker;

import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class PeerList {
	//TODO: vector constructor parameter tuning
	private final static int SEEDER_START = 64;
	private final static int SEEDER_GROW = 16;
	private final static int LEECHER_START = 64;
	private final static int LEECHER_GROW = 16;
	
	//TODO: initial size tuning		
	private ConcurrentHashMap<String, Vector<Peer>> seeders = 
		new ConcurrentHashMap<String, Vector<Peer>>(1024, 0.66F, 24);
	
	private ConcurrentHashMap<String, Vector<Peer>> leechers = 
		new ConcurrentHashMap<String, Vector<Peer>>(1024, 0.66F, 24);
	
	public PeerList() {}
	
	public void remove(final String info_hash, final String IP,
				final int port, final boolean isSeeder) {
		final Peer peer = new Peer();
		peer.setIpAddress(IP);
		peer.setLastAnnounce(new Date());
		peer.setPort(port);
		
		this.remove(info_hash, peer, isSeeder);
	}
	
	public void remove(final String info_hash, final Peer peer, final boolean isSeeder) {
		if(isSeeder) {
			if(this.seeders.get(info_hash) == null) {
				return;
			}
			
			this.seeders.get(info_hash).remove(peer);
		} else {
			if(this.leechers.get(info_hash) == null) {
				return;
			}
			
			this.leechers.get(info_hash).remove(peer);
		}
	}
	
	public void add(final String info_hash, final String IP, 
				final int port, final boolean isSeeder) {
		final Peer peer = new Peer();
		peer.setIpAddress(IP);
		peer.setLastAnnounce(new Date());
		peer.setPort(port);
		
		this.add(info_hash, peer, isSeeder);
	}
	
	public void add(final String info_hash, final Peer peer, final boolean isSeeder) {
		if(isSeeder) {
			if(this.seeders.get(info_hash) == null) {
				this.seeders.put(info_hash, new Vector<Peer>(SEEDER_START, SEEDER_GROW));
			}
			
			if(this.leechers.get(info_hash) != null && this.leechers.get(info_hash).contains(peer)) {
				this.leechers.get(info_hash).remove(peer);
			}
			
			if(this.seeders.get(info_hash).contains(peer) == false) {
				this.seeders.get(info_hash).add(peer);
			}
		} else {
			if(this.leechers.get(info_hash) == null) {
				this.leechers.put(info_hash, new Vector<Peer>(LEECHER_START, LEECHER_GROW));
			}
			
			if(this.seeders.get(info_hash).contains(peer)) {
				this.seeders.get(info_hash).remove(peer);
			}
			
			if(this.leechers.get(info_hash).contains(peer) == false) {
				this.leechers.get(info_hash).add(peer);
			}
		}
		
	}
	
	public Peer[] getSeeders(final String info_hash, final int numPeers) {
		final Vector<Peer> seeds = this.seeders.get(info_hash);
		final int size = seeds.size();
		
		//make sure there are more peers wanted than there are seeders
		//we need to go ahead and return this or we'll have a forever-true whileloop later.
		if(numPeers > size) {
			return this.seeders.get(info_hash).toArray(new Peer[size]);
		}
		
		//this is a sad little optimization that we have to prevent our while loop from taking
		//forever to complete.  We make sure that the peerlist is more than 10 peers larger
		//than the requested number.  We make "rand" so that there is some randomnity still in 
		//the peer retrieval.  Our cutoff points are when there are <10 more seeders than number of peers
		//or when the number of peers is more than 2/3 the complete size.
		if(numPeers + 10 > size || (numPeers*3)/2 > size) {
			final int rand = (new Random()).nextInt(size - numPeers);
			return this.seeders.get(info_hash).subList(rand, numPeers-rand).toArray(new Peer[numPeers]);
		}
		
		//This is a temporary hash-set that we use to store the peers we're going to be 
		//returning.  We use a hash set to ensure that peers don't get added more than once.
		HashSet<Peer> peers = new HashSet<Peer>(numPeers);
		
		//finally, we loop, generating random numbers, until we have enough peers
		while(peers.size() < numPeers) {
			final int rand = (new Random()).nextInt(size);
			peers.add(this.seeders.get(info_hash).get(rand));
		}
		
		return peers.toArray(new Peer[numPeers]);
	}
}
