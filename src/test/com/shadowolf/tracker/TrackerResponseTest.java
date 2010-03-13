package com.shadowolf.tracker;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.junit.Test;

import com.shadowolf.user.Peer;

public class TrackerResponseTest {
	@Test
	public void testBencodedString() {
		String testString = "d14:failure reason4:FAILe\r\n";
		String failure = TrackerResponse.bencoded("FAIL");
		assertTrue(testString.equals(failure));
	}
	
	@Test
	public void testCompactEncoding() {
		byte[] expected = new byte[] {
				(byte)0x7f,
				(byte)0xC9,
				(byte)0x01,
				(byte)0xff,
				(byte)0x00,
				(byte)0x3c
		};
		
		Peer peer = null;
		try {
			peer = new Peer(0L, 0L, "127.201.1.255", "60");
		} catch (UnknownHostException e1) {
			fail("Unexpected exception");
		}
		
		byte[] actual = new byte[0];
		
		actual = TrackerResponse.compactEncoding(peer);
		assertTrue(Arrays.equals(expected, actual));
		
		assertTrue(Arrays.equals(expected, actual));
	}


	@Test
	public void testBencodedCompactIntIntPeerarrIntInt() {
		int interval = 10;
		int minInterval = 100;
		int leechers = 100;
		int seeders = 100;
		Peer[] peers = null;
		try {
			peers = new Peer[] {
					new Peer(0L, 0L, "255.255.123.123", "65000"),
					new Peer(0L, 0L, "255.255.123.123", "65001")
			};
		} catch (UnknownHostException e1) {
			fail();
		}
		
		String resp = "d8:intervali" + interval + "e" + "12:min intervali" + minInterval + 
		"e10:incompletei" + leechers + "e8:completei" + seeders + "e";
		
		try {
			byte[] peerB = TrackerResponse.compact(peers);
			byte[] start = resp.getBytes("UTF-8");
			byte[] end = "e\r\n".getBytes("UTF-8");
			
			byte[] temp = new byte[start.length + peerB.length + end.length];
			System.arraycopy(start, 0, temp, 0, start.length);
			System.arraycopy(peerB, 0, temp, start.length, peerB.length);
			System.arraycopy(end, 0, temp, start.length + peerB.length, end.length);
			
			assertTrue(Arrays.equals(temp, TrackerResponse.bencoded(seeders, leechers, peers, interval, minInterval)));
		} catch (AnnounceException e) {
			fail("Unexpected AnnounceException");
		} catch (UnsupportedEncodingException e) {
			fail("Unexpected CharsetException");
		}
	}
	
	@Test
	public void testBencodedCompactIntIntPeerarr() {
		int leechers = 100;
		int seeders = 100;
		Peer[] peers = null;
		try {
			peers = new Peer[] {
					new Peer(0L, 0L, "255.255.123.123", "65000"),
					new Peer(0L, 0L, "255.255.123.123", "65001")
			};
		} catch (UnknownHostException e1) {
			fail("butts butts butts");
		}
		
		String resp = "d8:intervali" + TrackerResponse.DEFAULT_INTERVAL + "e" + "12:min intervali" + TrackerResponse.DEFAULT_MIN_INTERVAL + 
		"e10:incompletei" + leechers + "e8:completei" + seeders + "e";
		
		try {
			byte[] peerB = TrackerResponse.compact(peers);
			byte[] start = resp.getBytes("UTF-8");
			byte[] end = "e\r\n".getBytes("UTF-8");
			
			byte[] temp = new byte[start.length + peerB.length + end.length];
			System.arraycopy(start, 0, temp, 0, start.length);
			System.arraycopy(peerB, 0, temp, start.length, peerB.length);
			System.arraycopy(end, 0, temp, start.length + peerB.length, end.length);
			
			assertTrue(Arrays.equals(temp, TrackerResponse.bencoded(seeders, leechers, peers)));
		} catch (AnnounceException e) {
			fail("Unexpected AnnounceException");
		} catch (UnsupportedEncodingException e) {
			fail("Unexpected CharsetException");
		}
	}
	
	@Test
	public void testBencodedCompactIntIntPeerarrInt() {
		int interval = 10;
		int leechers = 100;
		int seeders = 100;
		Peer[] peers = null;
		try {
			peers = new Peer[] {
					new Peer(0l, 0l, "255.255.123.123", "65000"),
					new Peer(0l, 0l, "255.255.123.123", "65001")
			};
		} catch (UnknownHostException e1) {
			fail("butts butts butts");
		}
		
		String resp = "d8:intervali" + interval + "e" + "12:min intervali" + TrackerResponse.DEFAULT_MIN_INTERVAL + 
		"e10:incompletei" + leechers + "e8:completei" + seeders + "e";
		
		try {
			byte[] peerB = TrackerResponse.compact(peers);
			byte[] start = resp.getBytes("UTF-8");
			byte[] end = "e\r\n".getBytes("UTF-8");
			
			byte[] temp = new byte[start.length + peerB.length + end.length];
			System.arraycopy(start, 0, temp, 0, start.length);
			System.arraycopy(peerB, 0, temp, start.length, peerB.length);
			System.arraycopy(end, 0, temp, start.length + peerB.length, end.length);
			
			assertTrue(Arrays.equals(temp, TrackerResponse.bencoded(seeders, leechers, peers, interval)));
		} catch (AnnounceException e) {
			fail("Unexpected AnnounceException");
		} catch (UnsupportedEncodingException e) {
			fail("Unexpected CharsetException");
		}
	}
		
	@Test
	public void testCompactEncodingIPv6() {
		try {
			TrackerResponse.compactEncoding(new Peer(0l, 0l, "::1", "65000"));
			
		} catch (UnknownHostException e1) {
			fail("butts butts butts");
		}
	}

}