package com.shadowolf.tracker;

import java.io.UnsupportedEncodingException;

import com.shadowolf.util.Data;

public class CompactPeerEncoder {
	private byte[] IPv4 = new byte[0]; //NOPMD
	private byte[] IPv6 = new byte[0]; //NOPMD
	
	public void addToIPv4(final byte[] address) throws AnnounceException {
		if(address.length != 6) {
			throw new AnnounceException(TrackerResponse.Errors.UNEXPECTED_4_PEER_LENGTH.toString());
		}
		
		final byte[] temp = Data.addByteArrays(IPv4, address);
		IPv4 = temp;
	}
	
	public void addToIPv6(final byte[] address) throws AnnounceException {
		if(address.length != 18) {
			throw new AnnounceException(TrackerResponse.Errors.UNEXPECTED_6_PEER_LENGTH.toString());
		}
		
		final byte[] temp = Data.addByteArrays(IPv6, address);
		IPv6 = temp;
	}
	
	public byte[] encode() throws AnnounceException {
		try {
			byte[] peers = ("5:peers" + IPv4.length + ":").getBytes("UTF-8");
			byte[] peers6 = ("6:peers6" + IPv6.length + ":").getBytes("UTF-8");
			
			peers = Data.addByteArrays(peers, IPv4);
			peers6 = Data.addByteArrays(peers6, IPv6); 
			return Data.addByteArrays(peers, peers6);			
		} catch (UnsupportedEncodingException e) {
			throw new AnnounceException("Epic failure.", e);
		}
	}
}
