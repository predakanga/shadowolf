package com.shadowolf.tracker;

public class TrackerResponse {
	public static final int DEFAULT_MIN_INTERVAL = 600;
	public static final int DEFAULT_INTERVAL = 1800;
	
	public final static String bencoded(final String failure) {
		return "d14:failure reason" + failure.length() + ":" + failure + "e\r\n";
	}
	
	public final  static String bencoded(final int seeders, final int leechers, final String peers) {
		return bencoded(seeders, leechers, peers, DEFAULT_INTERVAL, DEFAULT_MIN_INTERVAL);
	}
	
	public final static String bencoded(final int seeders, final int leechers, final String peers, final int interval) {
		return bencoded(seeders, leechers, peers, interval, DEFAULT_MIN_INTERVAL);
	}
	
	public final static String bencoded(final int seeders, final int leechers, final String peers, final int interval, final int minInterval) {
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
		
		//for failure reason see makeResponse(String failure) {}
		
		String response = "d8:intervali" + interval + "e" + "12:min intervali" + minInterval + 
			"e10:incompletei" + leechers + "e8:completei" + seeders + "e5:peers" + 
			peers;
		
		response += "e\r\n";
		
		return response;
	}
}
