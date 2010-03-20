package com.shadowolf.user;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

final public class PeerListFactory {
	private PeerListFactory() {
		
	}
	
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
}
