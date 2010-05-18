package com.shadowolf.core.application.tracker.response;

import java.io.UnsupportedEncodingException;

import com.shadowolf.core.application.announce.AnnounceException;
import com.shadowolf.util.Data;

/**
 * Wrapper class for a compact-encoded (byte-form) peerlist.  Usage is simple: instantiate, add peers, call encode().
 */
public class CompactPeerEncoder {
	private byte[] IPv4 = new byte[0]; //NOPMD
	private byte[] IPv6 = new byte[0]; //NOPMD

	/**
	 * Adds an IPv4 address to the internal list of peers.  Expects a 6-length byte array.
	 * @param address the IPv4 address to add.
	 * @throws AnnounceException thrown if IPv4 address is not 6 bytes (4 for the address, 2 for the port).
	 */
	public void addToIPv4(final byte[] address) throws AnnounceException {
		if(address.length != 6) {
			throw new AnnounceException(Errors.UNEXPECTED_4_PEER_LENGTH);
		}

		final byte[] temp = Data.addByteArrays(this.IPv4, address);
		this.IPv4 = temp;
	}

	/**
	 * Adds an IPv6 address to the internal list of peers.  Expects an 18-length byte array.
	 * @param address the IPv6 address to add.
	 * @throws AnnounceException thrown if the IPv6 address is not 18 bytes (16 for the address, 2 for the port).
	 */
	public void addToIPv6(final byte[] address) throws AnnounceException {
		if(address.length != 18) {
			throw new AnnounceException(Errors.UNEXPECTED_6_PEER_LENGTH);
		}

		final byte[] temp = Data.addByteArrays(this.IPv6, address);
		this.IPv6 = temp;
	}

	/**
	 * Encodes the list as client-ready byte-array (includes bencode text). The return value is a byte array
	 * to prevent character encoding issues. 
	 * @return the byte array.
	 */
	public byte[] encode() {
		try {
			byte[] peers = ("5:peers" + this.IPv4.length + ":").getBytes("UTF-8");
			byte[] peers6 = ("6:peers6" + this.IPv6.length + ":").getBytes("UTF-8");

			peers = Data.addByteArrays(peers, this.IPv4);
			peers6 = Data.addByteArrays(peers6, this.IPv6);
			return Data.addByteArrays(peers, peers6);
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("This exception is actually impossible, silly java.", e);
		}
	}
}
