package com.shadowolf.tracker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

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
		
		Peer peer = new Peer("doesn't matter", "doesn't matter", 1245, "127.201.1.255", "60");
		byte[] actual = new byte[0];
		
		try {
			actual = TrackerResponse.compactEncoding(peer);
		} catch (AnnounceException e) {
			fail("Unexpected exception");
		}
		
		assertTrue(Arrays.equals(expected, actual));
	}

	@Test(expected=com.shadowolf.tracker.AnnounceException.class)
	public void testCompactEncodingExceptionSmallIP() throws AnnounceException {
		TrackerResponse.compactEncoding(new Peer("foo", "bar", 0, "127.0", "0"));
	}
	
	@Test(expected=com.shadowolf.tracker.AnnounceException.class)
	public void testCompactEncodingExceptionLargeIP() throws AnnounceException {
		TrackerResponse.compactEncoding(new Peer("foo", "bar", 0, "127.0.0.1.0", "0"));
	}

	@Test
	public void testBencodedIntIntString() {
		String expectedBencoded =
			"d8:interval" +
				"i" + TrackerResponse.DEFAULT_INTERVAL + "e" +
			"12:min interval" +
				"i" + TrackerResponse.DEFAULT_MIN_INTERVAL + "e" +
			"10:incomplete" +
				"i100e" +
			"8:complete" +
				"i100e" +
			"5:peers" +
				"BLAH" +
			"e\r\n";
		
		String actual = TrackerResponse.bencoded(100, 100, "BLAH");
		assertTrue(actual.equals(expectedBencoded));
	}

	@Test
	public void testBencodedIntIntStringInt() {
		String expectedBencoded =
			"d8:interval" +
				"i1600e" +
			"12:min interval" +
				"i" + TrackerResponse.DEFAULT_MIN_INTERVAL + "e" +
			"10:incomplete" +
				"i100e" +
			"8:complete" +
				"i100e" +
			"5:peers" +
				"BLAH" +
			"e\r\n";
		
		String actual = TrackerResponse.bencoded(100, 100, "BLAH", 1600);
		assertTrue(actual.equals(expectedBencoded));
	}

	@Test
	public void testBencodedIntIntStringIntInt() {
		String expectedBencoded =
			"d8:interval" +
				"i1600e" +
			"12:min interval" +
				"i1001e" +
			"10:incomplete" +
				"i100e" +
			"8:complete" +
				"i100e" +
			"5:peers" +
				"BLAH" +
			"e\r\n";
		
		String actual = TrackerResponse.bencoded(100, 100, "BLAH", 1600, 1001);
		assertTrue(actual.equals(expectedBencoded));
	}

	@Test
	public void testBencodedIntIntPeerarrIntInt() {
		int interval = 10;
		int minInterval = 100;
		int leechers = 100;
		int seeders = 100;
		Peer[] peers = new Peer[] {
				new Peer("foo", "bar", 12345, "255.255.123.123", "65000"),
				new Peer("foo", "bar", 12345, "255.255.123.123", "65001")
		};
		
		String resp = "d8:intervali" + interval + "e" + "12:min intervali" + minInterval + 
		"e10:incompletei" + leechers + "e8:completei" + seeders + "e5:peers";
		
		try {
			byte[] peerB = TrackerResponse.compact(peers);
			byte[] start = resp.getBytes("UTF-8");
			byte[] end = "e\r\n".getBytes("UTF-8");
			
			byte[] temp = new byte[start.length + peerB.length + end.length];
			System.arraycopy(start, 0, temp, 0, start.length);
			System.arraycopy(peerB, 0, temp, start.length, peerB.length);
			System.arraycopy(end, 0, temp, start.length + peerB.length, end.length);
			
			assertTrue(Arrays.equals(temp, TrackerResponse.bencodedCompact(seeders, leechers, peers, interval, minInterval)));
		} catch (AnnounceException e) {
			fail("Unexpected AnnounceException");
		} catch (UnsupportedEncodingException e) {
			fail("Unexpected CharsetException");
		}
	}
	
	@Test
	public void testBencodeCompact() {
		Peer peer = new Peer("doesn't matter", "doesn't matter", 1245, "127.201.1.255", "60");

		byte[] expected = new byte[0];
		
		try {
			expected = new byte[] {
					"6".getBytes("UTF-8")[0],
					":".getBytes("UTF-8")[0],
					(byte)0x7f,
					(byte)0xC9,
					(byte)0x01,
					(byte)0xff,
					(byte)0x00,
					(byte)0x3c
			};
		} catch (UnsupportedEncodingException e1) {
			fail("Unexpected AnnounceException");
		}
		
		byte[] actual = new byte[0];
		try {
			actual = TrackerResponse.bencodeCompact(new Peer[] { peer });
		} catch (UnsupportedEncodingException e) {
			fail("Unexpected AnnounceException");
		} catch (AnnounceException e) {
			fail("Unexpected CharsetException");
		}
		
		System.out.println(actual[0] & 0xFF);
		System.out.println(expected[0] & 0xFF);
		assertTrue(Arrays.equals(new byte[] { actual[2] } , new byte[] { expected[2] }));
	}
	
	@Test
	public void testBencodeFullNoPeerIds() {
		String expected =
			"l" +
				"d" +
					"2:ip" +
						"9:127.0.0.1" +
					"4:port" +
						"i47e" +
				"e" +
			"e\r\n";
		Peer[] peers = new Peer[] {
			new Peer("foo", "bar", 12345, "127.0.0.1", "47")	
		};
		
		String actual = "";
		try {
			actual = TrackerResponse.bencodeFull(peers, false);
		} catch (AnnounceException e) {
			fail("Unexpected AnnounceException");
		} catch (UnsupportedCharsetException e) {
			fail("Unexpected CharsetException");
		}
		
		assertTrue(actual.equals(expected));
	}
	
	@Test
	public void testBencodeFullPeerIds() {
		String peerId = "";
		try {
			peerId = URLEncoder.encode("fooooooooo#42csds!!#$", "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			fail("Unexpected EncodingException");
		}
		
		String expected =
			"l" +
				"d" +
					"7:peer_id" +
						peerId.length() + ":" + peerId + 
					"2:ip" +
						"9:127.0.0.1" +
					"4:port" +
						"i47e" +
					
				"e" +
			"e\r\n";
		Peer[] peers = new Peer[] {
			new Peer("foo", "fooooooooo#42csds!!#$", 12345, "127.0.0.1", "47")	
		};
		
		String actual = "";
		try {
			actual = TrackerResponse.bencodeFull(peers, true);
		} catch (AnnounceException e) {
			fail("Unexpected AnnounceException");
		} catch (UnsupportedCharsetException e) {
			fail("Unexpected CharsetException");
		}
		
		assertTrue(actual.equals(expected));
	}

	@Test
	public void testIsIPv6() {
		assertFalse(TrackerResponse.isIPv6("127.0.0.1"));
		assertTrue(TrackerResponse.isIPv6("::1"));
	}

}