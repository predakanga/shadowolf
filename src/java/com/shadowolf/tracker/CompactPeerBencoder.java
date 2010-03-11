package com.shadowolf.tracker;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.ArrayUtils;

public class CompactPeerBencoder {
	private byte[] IPv4 = new byte[0];
	private byte[] IPv6 = new byte[0];
	
	public void addToIPv4(byte[] address) throws AnnounceException {
		if(address.length != 6) {
			throw new AnnounceException("Unexpected IPv4 length");
		}
		
		byte[] temp = ArrayUtils.addAll(IPv4, address);
		IPv4 = temp;
	}
	
	public void addToIPv6(byte[] address) throws AnnounceException {
		if(address.length != 18) {
			throw new AnnounceException("Unexpected IPv4 length");
		}
		
		byte[] temp = ArrayUtils.addAll(IPv6, address);
		IPv6 = temp;
	}
	
	public byte[] encode() throws AnnounceException {
		try {
			byte[] peers = ("5:peers" + IPv4.length + ":").getBytes("UTF-8");
			byte[] peers6 = ("6:peers6" + IPv6.length + ":").getBytes("UTF-8");
			
			peers = ArrayUtils.addAll(peers, IPv4);
			peers6 = ArrayUtils.addAll(peers6, IPv6); 
			return ArrayUtils.addAll(peers, peers6);			
		} catch (UnsupportedEncodingException e) {
			throw new AnnounceException("Epic failure.");
		}
	}
}
