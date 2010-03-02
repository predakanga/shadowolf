package com.shadowolf.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PeerList extends WriteLockedStorage {
	public ArrayList<Integer> list = new ArrayList<Integer>();
	
	public PeerList(int[] peers) {
		this();
	
		for(int p : peers) {
			this.list.add(p);
		}
	}
	
	public PeerList() {	
	}
	
	public Boolean addPeer(int p) {
		if(this.contains(p)) {
			return true;
		}
		
		return this.list.add(p);
	}
	
	public Boolean contains(int p) {
		return this.list.contains(p);
	}
	
	public Integer remove(int p) {
		return this.list.remove(p);
	}
	
	public List<Integer> getPeerss() {
		return this.getPeers(20);
	}
	
	public List<Integer> getPeers(int num) {
		int max = (new Random()).nextInt(this.list.size() - num);
		int min = max - num + 1;
		return this.list.subList(min, max);
	}
}
