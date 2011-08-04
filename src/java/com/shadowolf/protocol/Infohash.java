package com.shadowolf.protocol;

import java.util.Arrays;

import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Charsets;

/**
 * This class represents a torrent's infohash (a sha1 hash
 * that uniquely identifies the torrent). This class is fully
 * thread-safe, as it is immutable.
 *
 */
@ThreadSafe
public class Infohash implements Comparable<Infohash> {
	final private byte[] data;
	
	/**
	 * Creates an Infohash with the String representation given, using
	 * UTF-8 encoding.
	 * @param s The representative String
	 */
	public Infohash(String s) {
		data = s.getBytes(Charsets.UTF_8);
	}
	
	/**
	 * Creates the infohash, loading it with the specified byte array. The 
	 * array given will be duplicated, and the copy will be stored.
	 * @param b The byte array representation
	 */
	public Infohash(byte[] b) {
		data = Arrays.copyOf(b, b.length);
	}
	
	/**
	 * Returns a UTF-8 encoded texual representation of the infohash.
	 * @return The textual representation of the infohash represented.
	 */
	@Override
	public String toString() {
		return new String(data,Charsets.UTF_8);
		
	}
	
	/**
	 * Returns a <i>copy</i> of the byte array representing this Infohash. This
	 * is only a copy of the array however, as Infohash is immutable.
	 * @return A copy of the byte representation
	 */
	public byte[] getBytes() {
		return Arrays.copyOf(data, data.length);
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
