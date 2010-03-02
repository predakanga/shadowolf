package com.shadowolf.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PeerList extends WriteLockedStorage {
	public ArrayList<Integer> list = new ArrayList<Integer>();
	
	public PeerList(int[] peers) {
		this();
	
		this.getLock(); 
		try {
			for(int p : peers) {
				this.list.add(p);
			}
		} finally {
			this.releaseLock();
		}
	}
	
	public PeerList() {	
	}
	
	public Boolean addPeer(int p) {
		Boolean status = false;
		if(this.contains(p)) {
			return true;
		}
		
		this.getLock();
		try {	
			status = this.list.add(p);
		} finally {
			this.releaseLock();	
		}
		
		return status;
	}
	
	public Boolean contains(int p) {
		return this.list.contains(p);
	}
	
	public Integer remove(int p) {
		this.getLock();
		try {
			return this.list.remove(p);
		} finally {
			this.releaseLock();
		}
	}
	
	public List<Integer> getPeers() {
		return this.getPeers(20);
	}
	
	public List<Integer> getPeers(int num) {
		int max = (new Random()).nextInt(this.list.size() - num);
		int min = max - num + 1;
		return this.list.subList(min, max);
	}
}
