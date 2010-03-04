package com.shadowolf.tracker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class PeerList {
	/*
	 * Peers maps
	 * 
	 * Peers is a map of maps.
	 * 
	 * The top level key->val is info_hash(String) -> Peer information(ConcurrentHashMap)
	 * The second level key->val is IP -> Port
	 */
	private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> seeders = 
		new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();
	
	private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> leechers = 
		new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();
	
	public void addUpdate(final String info_hash, final String IP, 
				final int port, final String event) {
			
	}
	
	@SuppressWarnings("unchecked")
	public ConcurrentHashMap<String, Integer>[] getSeeders(final String info_hash, final int numPeers) {
		final int size = this.seeders.get(info_hash).size();
		
		//make sure there are more peers wanted than there are seeders
		//we need to go ahead and return this or we'll have a forever-true whileloop later.
		if(numPeers > size) {
			return (ConcurrentHashMap<String, Integer>[]) this.seeders.get(info_hash).values().toArray();
		}
		
		//this is a sad little optimization that we have to prevent our while loop from taking
		//forever to complete.  We make sure that the peerlist is more than 10 peers larger
		//than the requested number.  We make "rand" so that there is some randomnity still in 
		//the peer retrieval.  Our cutoff points are when there are <10 more seeders than number of peers
		//or when the number of peers is more than 2/3 the complete size.
		if(numPeers + 10 > size || (numPeers*3)/2 > size) {
			final int rand = (new Random()).nextInt(size - numPeers);
			return (ConcurrentHashMap<String, Integer>[]) 
				Arrays.copyOfRange(this.seeders.get(info_hash).values().toArray(), rand, numPeers+rand-1);
		}
		
		//This is a temporary hash-set that we use to store the peers we're going to be 
		//returning.  We use a hash set to ensure that peers don't get added more than once.
		HashSet<ConcurrentHashMap<String, Integer>> peers = 
			new HashSet<ConcurrentHashMap<String,Integer>>(numPeers);
		
		//because there's no way to retrieve random values from a ConcurrentHashMap, nor
		//can we retrieve values by numerical key, instead we have to copy the array of values
		//entirely in order to accomplish that
		final ConcurrentHashMap<String, Integer>[] temp =
			(ConcurrentHashMap<String, Integer>[])this.seeders.get(info_hash).values().toArray();
		
		//finally, we loop, generating random numbers, until we have enough peers
		while(peers.size() < numPeers) {
			peers.add(temp[(new Random()).nextInt(temp.length) - 1]);
		}
		
		return (ConcurrentHashMap<String, Integer>[]) peers.toArray();
	}
}
