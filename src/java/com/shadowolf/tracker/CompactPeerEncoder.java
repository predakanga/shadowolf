package com.shadowolf.tracker;

import java.io.UnsupportedEncodingException;

import com.shadowolf.util.Data;

public class CompactPeerEncoder {
	private byte[] IPv4 = new byte[0]; //NOPMD
	private byte[] IPv6 = new byte[0]; //NOPMD

	public void addToIPv4(final byte[] address) throws AnnounceException {
		if(address.length != 6) {
			throw new AnnounceException(Errors.UNEXPECTED_4_PEER_LENGTH.toString());
		}

		final byte[] temp = Data.addByteArrays(this.IPv4, address);
		this.IPv4 = temp;
	}

	public void addToIPv6(final byte[] address) throws AnnounceException {
		if(address.length != 18) {
			throw new AnnounceException(Errors.UNEXPECTED_6_PEER_LENGTH.toString());
		}

		final byte[] temp = Data.addByteArrays(this.IPv6, address);
		this.IPv6 = temp;
	}

	public byte[] encode() throws AnnounceException {
		try {
			byte[] peers = ("5:peers" + this.IPv4.length + ":").getBytes("UTF-8");
			byte[] peers6 = ("6:peers6" + this.IPv6.length + ":").getBytes("UTF-8");

			peers = Data.addByteArrays(peers, this.IPv4);
			peers6 = Data.addByteArrays(peers6, this.IPv6);
			return Data.addByteArrays(peers, peers6);
		} catch (final UnsupportedEncodingException e) {
			throw new AnnounceException("Epic failure.", e);
		}
	}
}
