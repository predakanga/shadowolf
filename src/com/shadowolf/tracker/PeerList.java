package com.shadowolf.tracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;

public class PeerList {
	public static final int SEEDER = 0;
	public static final int LEECHER = 1;
	public static final int NOT_IN_LIST = 2;
	
	private static ConcurrentSkipListMap<Long, PeerList> lists = new ConcurrentSkipListMap<Long, PeerList>();
	private final static int SEEDER_START = 64;
	private final static int LEECHER_START = 16;
	
	private final static int DEFAULT_NUM_PEERS = 30;
	
	private ArrayList<Peer> seeders = new ArrayList<Peer>(SEEDER_START);
	private ArrayList<Peer> leechers = new ArrayList<Peer>(LEECHER_START);
	private long infoHash;
	
	public static PeerList getList(long infoHash) {
		if(lists.containsKey(infoHash) == false) {
			lists.put(infoHash, new PeerList(infoHash));
		}
		
		return lists.get(infoHash);
	}
	
	public static long collectUploaded(Iterator<Peer> iter) {
		long uploaded = 0;
		
		while(iter.hasNext()) {
			uploaded += iter.next().getUploaded();
		}
		
		return uploaded;
	}
	
	public static long collectDownloaded(Iterator<Peer> iter) {
		long downloaded = 0;
		
		while(iter.hasNext()) {
			downloaded += iter.next().getDownloaded();
		}
		
		return downloaded; 
	}
	
	private static Peer[] getPeers(final ArrayList<Peer> list, final int num) {
		if(list.size() == 0) {
			return new Peer[0];
		} else if (list.size() < num) {
			synchronized (list) {
				return list.toArray(new Peer[list.size()]);
			}
		}
		
		final int size = list.size();
		
		synchronized(list) {
			int rand = (new Random()).nextInt(size - num);
			return list.subList(rand, rand+num).toArray(new Peer[num]);
		}
	}
	
	private static boolean addToList(final ArrayList<Peer> list, final Peer p) {
		if(list.contains(p)) {
			return true; 
		} else {
			synchronized(list) {
				return list.add(p);
			}
		}
	}
	
	private static boolean removeFromList(final ArrayList<Peer> list, final Peer p) {
		synchronized(list) {
			return list.remove(p);
		}
	}
	
	private PeerList(long infoHash) {
		this.infoHash = infoHash;
	}
	
	public long getInfoHash() {
		return this.infoHash;
	}
	
	public boolean addSeeder(Peer p) {
		return addToList(this.seeders, p);
	}
	
	public boolean addLeecher(Peer p) {
		return addToList(this.leechers, p);
	}
	
	public boolean removeSeeder(Peer p) {
		return removeFromList(this.seeders, p);
	}
	
	public boolean removeLeech(Peer p) {
		return removeFromList(this.leechers, p);
	}
	
	public Peer[] getLeechers() {
		return getPeers(this.leechers, DEFAULT_NUM_PEERS);
	}
	
	public Peer[] getLeechers(final int num) {
		return getPeers(this.leechers, num);
	}
	
	public Peer[] getSeeders() {
		return getPeers(this.seeders, DEFAULT_NUM_PEERS);
	}
	
	public Peer[] getSeeders(final int num) {
		return getPeers(this.seeders, num);
	}
	
	public long getTotalUpload() {
		long up = 0;
		
		synchronized(this.seeders) {
			up += collectUploaded(this.seeders.iterator()); 
		}
		
		synchronized(this.leechers) {
			up += collectUploaded(this.leechers.iterator());
		}
		
		return up;
	}
	
	public synchronized long getTotalDownload() {
		long down = 0;
		
		synchronized(this.seeders) {
			down += collectDownloaded(this.seeders.iterator()); 
		}
		
		synchronized(this.leechers) {
			down += collectDownloaded(this.leechers.iterator());
		}
		
		return down;
	}
	
	public int contains(Peer p) {
		if(this.seeders.contains(p)) {
			return SEEDER;
		}
		
		if(this.leechers.contains(p)) {
			return LEECHER;
		}
		
		return NOT_IN_LIST;
	}
	
	
	
}