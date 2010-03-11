package com.shadowolf.tracker;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import org.apache.commons.lang.ArrayUtils;

/* http://wiki.theory.org/BitTorrentSpecification#Tracker_Response
 * failure reason: If present, then no other keys may be present. The value is a human-readable error message as to why the request failed (string).
 * warning message: (new, optional) Similar to failure reason, but the response still gets processed normally. The warning message is shown just like an error.
 * interval: Interval in seconds that the client should wait between sending regular requests to the tracker
 * min interval: (optional) Minimum announce interval. If present clients must not reannounce more frequently than this.
 * tracker id: A string that the client should send back on its next announcements. If absent and a previous announce sent a tracker id, do not discard the old value; keep using it.
 * complete: number of peers with the entire file, i.e. seeders (integer)
 * incomplete: number of non-seeder peers, aka "leechers" (integer)
 * peers: (dictionary model) The value is a list of dictionaries, each with the following keys:
      o peer id: peer's self-selected ID, as described above for the tracker request (string)
      o ip: peer's IP address either IPv6 (hexed) or IPv4 (dotted quad) or DNS name (string)
      o port: peer's port number (integer) 
  * peers: (binary model) Instead of using the dictionary model described above, the peers value may be a string consisting of multiples of 6 bytes. First 4 bytes are the IP address and last 2 bytes are the port number. All in network (big endian) notation.
 */

final public class TrackerResponse {
	public static final int DEFAULT_MIN_INTERVAL = 600;
	public static final int DEFAULT_INTERVAL = 1800;
	
	public final static String bencoded(final String failure) {
		return "d14:failure reason" + failure.length() + ":" + failure + "e\r\n";
	}
	
	public final static String bencoded(final int seeders, final int leechers, final Peer[] peers) throws AnnounceException {
		return bencoded(seeders, leechers, full(peers, false), DEFAULT_INTERVAL);
	}
	
	public final static String bencoded(final int seeders, final int leechers, final Peer[] peers, final int interval) throws AnnounceException {
		return bencoded(seeders, leechers, full(peers, false), interval, DEFAULT_MIN_INTERVAL);
	}
	
	public final static String bencoded(final int seeders, final int leechers, final Peer[] peers, final int interval, final int minInterval) throws AnnounceException {
		return bencoded(seeders, leechers, full(peers, false), interval, minInterval);
	}
	
	
	public final static String bencoded(final int seeders, final int leechers, final String peers) {
		return bencoded(seeders, leechers, peers, DEFAULT_INTERVAL, DEFAULT_MIN_INTERVAL);
	}
	
	public final static String bencoded(final int seeders, final int leechers, final String peers, final int interval) {
		return bencoded(seeders, leechers, peers, interval, DEFAULT_MIN_INTERVAL);
	}
	
	public static final byte[] bencodedCompact(final int seeders, final int leechers, final Peer[] peers) throws AnnounceException {
		return bencodedCompact(seeders, leechers, peers, DEFAULT_INTERVAL, DEFAULT_MIN_INTERVAL);
	}
	
	public static final byte[] bencodedCompact(final int seeders, final int leechers, final Peer[] peers, final int interval) throws AnnounceException {
		return bencodedCompact(seeders, leechers, peers, interval, DEFAULT_MIN_INTERVAL);
	}
	
	public final static String bencoded(final int seeders, final int leechers, final String peers, final int interval, final int minInterval) {
		String response = "d8:intervali" + interval + "e" + "12:min intervali" + minInterval + 
			"e10:incompletei" + leechers + "e8:completei" + seeders + "e5:peers" + 
			peers;
		
		response += "e\r\n";
		
		return response;
	}
	
	public static final byte[] bencodedCompact(final int seeders, final int leechers, final Peer[] peers, final int interval, final int minInterval) throws AnnounceException {
		try {
			final byte[] enc = compact(peers);
			final byte[] start = ("d8:intervali" + interval + "e" + "12:min intervali" + minInterval + 
				"e10:incompletei" + leechers + "e8:completei" + seeders + "e").getBytes("UTF-8");
			final byte[] end = "e\r\n".getBytes("UTF-8");
			
			byte[] temp = ArrayUtils.addAll(start, enc);
			return ArrayUtils.addAll(temp, end);
		} catch (UnsupportedEncodingException e) {
			throw new AnnounceException("There was a problem encoding characters.  Contact your site administrator");
		}
	}
	
	public final static byte[] compact(Peer[] peers) throws AnnounceException, UnsupportedEncodingException {
		CompactPeerBencoder cpb = new CompactPeerBencoder();
		
		for(Peer p : peers) {
			if(isIPv6(p.getIpAddress())) {
				cpb.addToIPv6(compactEncoding(p));
			} else {
				cpb.addToIPv4(compactEncoding(p));
			}
		}
		
		return cpb.encode();
	}
	
	public final static String full(Peer[] peers, boolean noPeerIDs) throws AnnounceException {
		//5:peersld7:peer id4:fuck2:ip15:0:0:0:0:0:0:0:14:porti123eee (with one peer)?

		String buffer = "l"; //open peerlist list
		
		for(Peer p : peers) {
			buffer += "d"; //open peer dictionary
			
			if(noPeerIDs != false) {
				try {
					final String peerId = URLEncoder.encode(p.getPeerId(), "UTF-8");
					buffer += "7:peer_id" + peerId.length() + ":" + peerId;
				} catch (UnsupportedEncodingException e) {
					throw new AnnounceException("There was a character encoding exception.  Please contact your site administrator.");
				}
			}
			
			buffer += "2:ip" + p.getIpAddress().length() + ":" + p.getIpAddress();
			buffer += "4:porti" + p.getPort() + "e";
			buffer += "e"; //close peer dictionary
		}
		
		buffer += "e"; //close peerlist list
		return buffer;
	}
	
	public final static byte[] compactEncoding(Peer p) throws AnnounceException {
		String IP = p.getIpAddress();
		int port = new Integer(p.getPort());
		
		byte[] portArr =  new byte[] {
				(byte)(((byte)(port >>> 8)) & 0xff),
				(byte)((byte)port & 0xff),
		};
		
		if(isIPv6(IP) == false) {			
			try {
				byte[] IPArr = Inet4Address.getByName(IP).getAddress();
				
				if(IPArr.length != 4) {
					throw new AnnounceException("Error parsing IP.");
				}
				
				byte[] address = new byte[6];
				System.arraycopy(IPArr, 0, address, 0, 4);
				System.arraycopy(portArr, 0, address, 4, 2);
				return address;
			} catch (UnknownHostException e) {
				throw new AnnounceException("Error parsing IP.");
			}
		} else {
			
			
			try {
				byte[] IPArr = Inet6Address.getByName(IP).getAddress();
				
				if(IPArr.length != 16) {
					throw new AnnounceException("Error parsing IP.");
				}
				
				byte[] address = new byte[18];
				System.arraycopy(IPArr, 0, address, 0, 16);
				System.arraycopy(portArr, 0, address, 16, 2);
				return address;
			} catch (UnknownHostException e) {
				throw new AnnounceException("Error parsing IP.");
			}
		}
		
		//return new byte[] {};
	}
	
	public final static boolean isIPv6(final String IP) {
    	return IP != null && IP.contains(":");
    }
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		URLEncoder.encode(null, "UTF-8");
	}
}