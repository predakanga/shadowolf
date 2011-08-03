package com.shadowolf.protocol;

import java.util.Arrays;
import com.google.common.base.Charsets;

//TODO comparable?
public class Infohash implements Comparable<Infohash> {
	final private byte[] data;
	
	public Infohash(String s) {
		data = s.getBytes(Charsets.UTF_8);
	}
	
	public Infohash(byte[] b) {
		data = b;
	}
	
	@Override
	public String toString() {
		return new String(data,Charsets.UTF_8);
		
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Infohash) {
			return Arrays.equals(data, ((Infohash) o).data);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	@Override
	public int compareTo(Infohash o) {
		if(o == null) {
			return -1;
		}
		
		return hashCode() - o.hashCode();
	}

}
