package com.shadowolf.tracker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
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
			assertTrue(Arrays.equals(expected, actual));
		} catch (AnnounceException e) {
			fail("Unexpected exception");
		}
		
		assertTrue(Arrays.equals(expected, actual));
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
	public void testBencodedCompactIntIntPeerarrIntInt() {
		int interval = 10;
		int minInterval = 100;
		int leechers = 100;
		int seeders = 100;
		Peer[] peers = new Peer[] {
				new Peer("foo", "bar", 12345, "255.255.123.123", "65000"),
				new Peer("foo", "bar", 12345, "255.255.123.123", "65001")
		};
		
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
			
			assertTrue(Arrays.equals(temp, TrackerResponse.bencodedCompact(seeders, leechers, peers, interval, minInterval)));
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
		Peer[] peers = new Peer[] {
				new Peer("foo", "bar", 12345, "255.255.123.123", "65000"),
				new Peer("foo", "bar", 12345, "255.255.123.123", "65001")
		};
		
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
			
			assertTrue(Arrays.equals(temp, TrackerResponse.bencodedCompact(seeders, leechers, peers)));
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
		Peer[] peers = new Peer[] {
				new Peer("foo", "bar", 12345, "255.255.123.123", "65000"),
				new Peer("foo", "bar", 12345, "255.255.123.123", "65001")
		};
		
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
			
			assertTrue(Arrays.equals(temp, TrackerResponse.bencodedCompact(seeders, leechers, peers, interval)));
		} catch (AnnounceException e) {
			fail("Unexpected AnnounceException");
		} catch (UnsupportedEncodingException e) {
			fail("Unexpected CharsetException");
		}
	}
	
	@Test
	public void testBencodedIntIntPeerarr() {
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
				"l" +
					"d" + 
						//"7:peer_id" + "1:a" +
						"2:ip" + "9:127.0.0.1" +
						"4:port" + "i60e" +
					"e" +
				"e" +
			"e\r\n";
		
		String actual = "";
		try {
			actual = TrackerResponse.bencoded(100, 100, new Peer[] {
					new Peer("a", "a", 123, "127.0.0.1", "60")
			});
			
			
			
		} catch (AnnounceException e) {
			fail("Unexpected AnnounceException");
		}
		
		assertTrue(actual.equals(expectedBencoded));
	}
	
	@Test
	public void testBencodedIntIntPeerarrInt() {
		String expectedBencoded =
			"d8:interval" +
				"i" + 27 + "e" +
			"12:min interval" +
				"i" + TrackerResponse.DEFAULT_MIN_INTERVAL + "e" +
			"10:incomplete" +
				"i100e" +
			"8:complete" +
				"i100e" +
			"5:peers" +
				"l" +
					"d" + 
						//"7:peer_id" + "1:a" +
						"2:ip" + "9:127.0.0.1" +
						"4:port" + "i60e" +
					"e" +
				"e" +
			"e\r\n";
		
		String actual = "";
		try {
			actual = TrackerResponse.bencoded(100, 100, new Peer[] {
					new Peer("a", "a", 123, "127.0.0.1", "60")
			}, 27);
			
			
			
		} catch (AnnounceException e) {
			fail("Unexpected AnnounceException");
		}
		
		assertTrue(actual.equals(expectedBencoded));
	}
	
	@Test
	public void testBencodedIntIntPeerarrIntInt() {
		String expectedBencoded =
			"d8:interval" +
				"i" + 27 + "e" +
			"12:min interval" +
				"i" + 111 + "e" +
			"10:incomplete" +
				"i100e" +
			"8:complete" +
				"i100e" +
			"5:peers" +
				"l" +
					"d" + 
						//"7:peer_id" + "1:a" +
						"2:ip" + "9:127.0.0.1" +
						"4:port" + "i60e" +
					"e" +
				"e" +
			"e\r\n";
		
		String actual = "";
		try {
			actual = TrackerResponse.bencoded(100, 100, new Peer[] {
					new Peer("a", "a", 123, "127.0.0.1", "60")
			}, 27, 111);
			
			
			
		} catch (AnnounceException e) {
			fail("Unexpected AnnounceException");
		}
		
		assertTrue(actual.equals(expectedBencoded));
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
			"e";
		Peer[] peers = new Peer[] {
			new Peer("foo", "bar", 12345, "127.0.0.1", "47")	
		};
		
		String actual = "";
		try {
			actual = TrackerResponse.full(peers, false);
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
			"e";
		Peer[] peers = new Peer[] {
			new Peer("foo", "fooooooooo#42csds!!#$", 12345, "127.0.0.1", "47")	
		};
		
		String actual = "";
		try {
			actual = TrackerResponse.full(peers, true);
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
	
	@Test
	public void testCompactEncodingIPv6() {
		try {
			TrackerResponse.compactEncoding(new Peer("a", "b", 1234, "::1", "65000"));
		} catch (AnnounceException e) {
			fail("Unexpected AnnounceException");
		}
	}

}