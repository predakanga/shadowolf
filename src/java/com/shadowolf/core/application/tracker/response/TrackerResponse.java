package com.shadowolf.core.application.tracker.response;

import java.io.UnsupportedEncodingException;

import com.shadowolf.core.application.announce.AnnounceException;
import com.shadowolf.core.application.tracker.ClientIdentifier;
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

/**
 * Encapsulates logic used to create a response from the tracker to the user.
 */
public class TrackerResponse {
	private static byte[] compactAnnounce(final ClientIdentifier[] clients) throws AnnounceException {
		final CompactPeerEncoder cpb = new CompactPeerEncoder();

		for (final ClientIdentifier p : clients) {
			if (p.getIpAddress().length > 4) {
				cpb.addToIPv6(compactEncodingAnnounce(p));
			} else {
				cpb.addToIPv4(compactEncodingAnnounce(p));
			}
		}

		return cpb.encode();
	}

	private static byte[] compactEncodingAnnounce(final ClientIdentifier peer) {
		return Data.addByteArrays(peer.getIpAddress(), peer.getPort());
	}

	private final int interval;
	private final int minInterval;

	public TrackerResponse(final int interval, final int minInterval) {
		this.interval = interval;
		this.minInterval = minInterval;
	}

	/**
	 * Bencodes a client-ready announce as a byte array.
	 * 
	 * @param seeders
	 *            the number of seeders, this class has no way to tell whether a
	 *            clientIdentifier is a seeder or not.
	 * @param leechers
	 *            the number of leechers.
	 * @param clients
	 *            the list of {@link ClientIdentifier} instances that make up
	 *            what peers to send back.
	 * @return A nice little announce, with a bow. A red bow. Made of shiny
	 *         ribbon.
	 * @throws AnnounceException
	 */
	public byte[] bencodedAnnounce(final int seeders, final int leechers, final ClientIdentifier[] clients)
			throws AnnounceException {
		try {
			final byte[] start = ("d8:intervali" + this.interval + "e" + "12:min intervali" + this.minInterval +
					"e10:incompletei" + leechers + "e8:completei" + seeders + "e").getBytes("UTF-8");
			final byte[] temp = Data.addByteArrays(start, compactAnnounce(clients));
			return Data.addByteArrays(temp, "e\r\n".getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			// UNPOSSIBLE!
			return null;
		}
	}

	/**
	 * Bencodes a failure reason and returns the bencoded (client-ready) result.
	 * 
	 * @param reason
	 *            The failure reason; this will appear as human readable in a
	 *            client.
	 * @return a client-ready bencoded string.
	 */
	public static String bencodeFailure(final String reason) {
		return "d14:failure reason" + reason.length() + ":" + reason + "e\r\n";
	}

}
