package com.shadowolf.protocol;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import com.shadowolf.protocol.Peer;
import org.junit.Test;

public class AnnounceResponseTests {

	@Test
	public void testResponseLengthWith4ipv4() {
		List<Peer> peers = new LinkedList<>();

		peers.add(new Peer(new InetSocketAddress("127.0.0.1",1547)));
		peers.add(new Peer(new InetSocketAddress("127.0.0.2",2547)));
		peers.add(new Peer(new InetSocketAddress("127.0.0.3",1387)));
		peers.add(new Peer(new InetSocketAddress("127.0.0.4",1557)));
		
		Peer[] p = new Peer[4];
		peers.toArray(p);
		AnnounceResponse a = new AnnounceResponse(4, 0, p, 900, 1800);
		
		String result = a.bencode();
		int expected = calculateExpected(4,0);
		int actual = result.length();
		assertTrue("The size of the ipv4 array result should be "+expected+" but it was "+actual+". ("+result+")",actual==expected);
	}
	
	@Test
	public void testResponseWith4ipv4() {
		List<Peer> peers = new LinkedList<>();

		peers.add(new Peer(new InetSocketAddress("127.0.0.1",1547)));
		peers.add(new Peer(new InetSocketAddress("127.0.0.2",2547)));
		peers.add(new Peer(new InetSocketAddress("127.0.0.3",1387)));
		peers.add(new Peer(new InetSocketAddress("127.0.0.4",1557)));
		
		Peer[] p = new Peer[4];
		peers.toArray(p);
		int interval = 900;
		int minInterval = 1800;
		int seeders = 2;
		int leechers = 2;
		String exp = "d8:intervali" + interval + "e" + "12:min intervali" + minInterval + "e10:incompletei" + leechers + "e8:completei" + seeders + "e5:peers24:";

		AnnounceResponse a = new AnnounceResponse(seeders, leechers, p, interval, minInterval);

		System.out.println(exp.length()); // length is 76
		
		for(int i=0; i < p.length; i++) {
			exp += new String(p[i].getAddress().getAddress().getAddress());
			exp += new String(intToByte(p[i].getAddress().getPort()));
		}
		exp = exp + "e\r\n";

		System.out.println(exp.length());
		
		String result = a.bencode();
		assertTrue("("+exp.length()+" vs "+result.length()+") The result was expected to be "+exp+" but it was "+result+".", exp.equals(result));
	}
	private static int calculateExpected(int ipv4, int ipv6) {
		// this is wrongish
		return 66 + 3 + (ipv4>0?10:0) + (ipv6>0?11:0) + (ipv4*6) + (ipv6*16);
	}
	
	private static byte[] intToByte(int i) {
		return new byte[] { (byte)(i >>> 8), (byte) i};
	}
}
