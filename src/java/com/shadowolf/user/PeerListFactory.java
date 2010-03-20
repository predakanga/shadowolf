package com.shadowolf.user;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

final public class PeerListFactory {
	static ConcurrentHashMap<String, PeerList> lists = new ConcurrentHashMap<String, PeerList>();
	
	public static PeerList getList(String infoHash) {
		if(lists.get(infoHash) == null) {
			PeerList p =  new PeerList();
			lists.put(infoHash, p);
			return p;
		} else {
			return lists.get(infoHash);
		}
	}
	
	public static void cleanUp() {
		Iterator<String> i = lists.keySet().iterator();
		
		while(i.hasNext()) {
			lists.get(i.next()).doCleanUp();
		}
	}
}
