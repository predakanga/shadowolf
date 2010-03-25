package com.shadowolf.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.shadowolf.util.Data;

final public class PeerListFactory {
	private PeerListFactory() {
		
	}
	private final static boolean DEBUG = true;
	private final static Logger LOGGER = Logger.getLogger(PeerListFactory.class);
	private final static ConcurrentHashMap<String, PeerList> LISTS = new ConcurrentHashMap<String, PeerList>();
	
	public static PeerList getList(final String infoHash) {
		if(LISTS.get(infoHash) == null) {
			final PeerList peers =  new PeerList();
			LISTS.put(infoHash, peers);
			return peers; //NOPMD ... the other option is to do a check, no.
		} else {
			return LISTS.get(infoHash); //NOPDM
		}
	}
	
	public static void cleanUp() {
		final Iterator<String> iter = LISTS.keySet().iterator();
		
		while(iter.hasNext()) {
			LISTS.get(iter.next()).doCleanUp();
		}
	}
	
	public static HashMap<byte[], int[]> getPeerCounts() {
		final Iterator<String> iter = LISTS.keySet().iterator();
		if(DEBUG) {
			LOGGER.debug("Collecting peer counts for ... " + LISTS.size());
		}
		
		final HashMap<byte[], int[]> counts = new HashMap<byte[], int[]>();
		while(iter.hasNext()) {
			final String hash = iter.next();
			if(DEBUG) {
				LOGGER.debug("Collecting peer counts for ... " + hash);
			}
			
			final int[] peers = new int[2];
			peers[0] = LISTS.get(hash).getSeederCount();
			peers[1] = LISTS.get(hash).getLeecherCount();
			counts.put(Data.hexStringToByteArray(hash), peers);
		}
		
		return counts;
	}
}
