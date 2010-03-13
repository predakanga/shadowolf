package com.shadowolf.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


public class PeerList {
	private static final Logger LOGGER = Logger.getLogger(PeerList.class);
	
	public static final int SEEDER = 0;
	public static final int LEECHER = 1;
	public static final int NOT_IN_LIST = 2;
	
	private static ConcurrentHashMap<byte[], PeerList> lists = new ConcurrentHashMap<byte[], PeerList>();
	private final static int SEEDER_START = 64;
	private final static int LEECHER_START = 16;
	
	private final static int DEFAULT_NUM_PEERS = 30;
	
	private ArrayList<Peer> seeders = new ArrayList<Peer>(SEEDER_START);
	private ArrayList<Peer> leechers = new ArrayList<Peer>(LEECHER_START);
	
	public static PeerList getList(byte[] infoHash2) {
		if(lists.containsKey(infoHash2) == false) {
			lists.put(infoHash2, new PeerList());
		}
		
		return lists.get(infoHash2);
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
	
	private PeerList() {
	}
		
	public boolean addSeeder(Peer p) {
		if(this.seeders.contains(p)) {
			return true;
		}
		
		boolean status = false;
		synchronized (this.seeders) {
			status = this.seeders.add(p);
		}
		
		LOGGER.debug("Added seeder.  Total: " + this.seeders.size());
		return status;
	}
	
	public boolean addLeecher(Peer p) {
		if(this.leechers.contains(p)) {
			return true;
		}
		
		boolean status = false;

		synchronized (this.leechers) {
			status = this.leechers.add(p);
		}
		
		LOGGER.debug("Added leecher.  Total: " + this.leechers.size());
		return status;
	}
	
	public boolean removeLeecher(Peer p) {
		if(this.leechers.contains(p) == false) {
			return true;
		}
		
		boolean status = false;
		synchronized (this.leechers) {
			status = this.leechers.remove(p);
		}
		
		LOGGER.debug("Removed leecher.  Total: " + this.leechers.size());
		return status;
	}
	
	public boolean removeSeeder(Peer p) {
		if(this.seeders.contains(p) == false) {
			LOGGER.debug("Not removing because of non-existance");
			return true;
		}
		
		boolean status = false;
		synchronized (this.seeders) {
			status = this.seeders.remove(p);
		}
		
		LOGGER.debug("Removed seeder.  Total: " + this.seeders.size());
		return status;
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