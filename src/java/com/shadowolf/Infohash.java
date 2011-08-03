package com.shadowolf;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.shadowolf.util.Exceptions;

//TODO comparable?
public class Infohash {
	final private byte[] data;
	
	public Infohash(String s) {
		try {
			data = s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			Exceptions.log(e);
			throw new RuntimeException("Error converting infohash -- see log.");
		}
	}
	
	public Infohash(byte[] b) {
		data = b;
	}
	
	@Override
	public String toString() {
		try {
			return new String(data,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			Exceptions.log(e);
			throw new RuntimeException("Error converting infohash -- see log.");
		}
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
		return toString().hashCode();
	}

}
