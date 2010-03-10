package com.shadowolf.tracker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
		return bencoded(seeders, leechers, full(peers, false), DEFAULT_INTERVAL, DEFAULT_MIN_INTERVAL);
	}
	
	public final static String bencoded(final int seeders, final int leechers, final Peer[] peers, final int interval) throws AnnounceException {
		return bencoded(seeders, leechers, full(peers, false), interval, DEFAULT_MIN_INTERVAL);
	}
	
	public final static String bencoded(final int seeders, final int leechers, final Peer[] peers, final int interval, final int minInterval) throws AnnounceException {
		return bencoded(seeders, leechers, full(peers, false), interval, DEFAULT_MIN_INTERVAL);
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
			final byte[] start = ("d8:intervali" + interval + "e" + "12:min intervali" + minInterval + 
				"e10:incompletei" + leechers + "e8:completei" + seeders + "e5:peers").getBytes("UTF-8");
			final byte[] enc = compact(peers);
			final byte[] end = "e\r\n".getBytes("UTF-8");
			final byte[] buffer = new byte[start.length + enc.length + end.length];
			System.arraycopy(start, 0, buffer, 0, start.length);
			System.arraycopy(enc, 0, buffer, start.length, enc.length);
			System.arraycopy(end, 0, buffer, start.length + enc.length, end.length);
			return buffer;
		} catch (UnsupportedEncodingException e) {
			throw new AnnounceException("There was a problem encoding characters.  Contact your site administrator");
		}
	}
	
	public final static byte[] compact(Peer[] peers) throws AnnounceException, UnsupportedEncodingException {
		byte[] start = new byte[] {
				"6".getBytes("UTF-8")[0],
				":".getBytes("UTF-8")[0]
		};
		
		byte[] buffer = new byte[start.length];
		
		System.arraycopy(start, 0, buffer, 0, start.length);
		
		System.out.println(buffer[1]);
		for(Peer p : peers) {
			final byte[] enc = compactEncoding(p);
			final byte[] temp = new byte[buffer.length + enc.length];
			System.arraycopy(buffer, 0, temp, 0, buffer.length);
			System.arraycopy(enc, 0, temp, buffer.length, enc.length);
			buffer = temp;
		}
		
		return buffer;
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
		
		buffer += "e\r\n"; //close peerlist list
		return buffer;
	}
	
	public final static byte[] compactEncoding(Peer p) throws AnnounceException {
		if(isIPv6(p.getIpAddress()) == false) {
			String[] octets = p.getIpAddress().split("\\.");

			if(octets.length != 4) {
				throw new AnnounceException("There was a problem encoding peer IP address.");
			}
			
			int port = new Integer(p.getPort());
			return new byte[] {
					((byte) (Integer.parseInt(octets[0]) & 0xFF)),
					((byte) (Integer.parseInt(octets[1]) & 0xFF)),
					((byte) (Integer.parseInt(octets[2]) & 0xFF)),
					((byte) (Integer.parseInt(octets[3]) & 0xFF)),
					(byte)( ((byte)(port >>> 8)) & 0xff),
					(byte)((byte)port & 0xff),
			};
		} else {
			throw new AnnounceException("IPv6 is not yet supported");
		}
		
		//return new byte[] {};
	}
	
	public final static boolean isIPv6(final String IP) {
    	return IP != null && IP.contains(":");
    }
	
}
