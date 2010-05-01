/**
 * 
 */
package com.shadowolf.tracker;

import com.shadowolf.config.Config;

public enum Errors {
	TORRENT_NOT_REGISTERED("Torrent is not registered with this tracker"),
	INVALID_PASSKEY("Unrecognized passkey"),
	UNPARSEABLE_INFO_HASH("Failed to parse the info-hash your client passed."),
	BANNED_CLIENT("Your client is banned.  Please upgrade to a client on the whitelist."),
	MISSING_PORT("Your client did not send required parameter: port"),
	MISSING_INFO_HASH("Your client did not send required parameter: info_hash"),
	MISSING_PASSKEY("Your client did not send required parameter: passkey"),
	MISSING_PEER_ID("Your client did not send required parameter: peer_id"),
	TOO_MANY_LOCATIONS("You're already seeding from " + Config.getParameter("user.max_locations") + " locations"),
	UNEXPECTED_4_PEER_LENGTH("IPv4 address: unexpected length"),
	UNEXPECTED_6_PEER_LENGTH("IPv6 address: unexpected length");
	
	private final String humanReadable;

	private Errors(final String humanReadable) {
		this.humanReadable = TrackerResponse.bencoded(humanReadable);
	}

	public String toString() {
		return this.humanReadable;
	}
}