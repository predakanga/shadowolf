package com.shadowolf.tracker;

import java.io.UnsupportedEncodingException;

import com.shadowolf.config.Config;
import com.shadowolf.user.Peer;
import com.shadowolf.util.Data;

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

final public class TrackerResponse { //NOPMD ... not too many methods when they're overloaded!
	private TrackerResponse() {

	}

	public static String bencoded(final String failure) {
		return "d14:failure reason" + failure.length() + ":" + failure + "e\r\n";
	}
	
	public static StringBuilder wrapInDict(final StringBuilder builder, final String key) {
		return builder.insert(0, "d"+key.length()+":"+key).append("e");
	}
	
	public static StringBuilder bencodedScrape(final int seeders, final int leechers, final int completed, final String infohash, final StringBuilder builder) {
		return builder.append("d20:" + infohash + "d8:completei" + seeders + "e10:downloadedi" + completed + "e10:incompletei" + leechers + "eee");
	}

	public static byte[] bencodedAnnounce(final int seeders, final int leechers, final Peer[] peers) throws AnnounceException {
		return bencodedAnnounce(seeders, leechers, peers, Integer.parseInt(Config.getParameter("tracker.interval")), Integer.parseInt(Config.getParameter("tracker.min_interval")));
	}

	public static byte[] bencodedAnnounce(final int seeders, final int leechers, final Peer[] peers, final int interval, final int minInterval) throws AnnounceException {
		try {
			final byte[] start = ("d8:intervali" + interval + "e" + "12:min intervali" + minInterval +
					"e10:incompletei" + leechers + "e8:completei" + seeders + "e").getBytes("UTF-8");

			final byte[] temp = Data.addByteArrays(start, compactAnnounce(peers));
			return Data.addByteArrays(temp, "e\r\n".getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			throw new AnnounceException("Epic failure", e);
		}
	}

	public static byte[] compactAnnounce(final Peer[] peers) throws AnnounceException, UnsupportedEncodingException {
		final CompactPeerEncoder cpb = new CompactPeerEncoder();

		for(final Peer p : peers) {
			if(p.getIpAddress().length > 4) {
				cpb.addToIPv6(compactEncodingAnnounce(p));
			} else {
				cpb.addToIPv4(compactEncodingAnnounce(p));
			}
		}

		return cpb.encode();
	}

	public static byte[] compactEncodingAnnounce(final Peer peer) {
		final byte[] portArr = peer.getPort();
		final byte[] IPArr = peer.getIpAddress();

		if(IPArr.length == 4) {
			final byte[] address = new byte[6];
			System.arraycopy(IPArr, 0, address, 0, 4);
			System.arraycopy(portArr, 0, address, 4, 2);
			return address; //NOPMD
		} else {
			final byte[] address = new byte[18];
			System.arraycopy(IPArr, 0, address, 0, 16);
			System.arraycopy(portArr, 0, address, 16, 2);
			return address;
		}
	}
}
