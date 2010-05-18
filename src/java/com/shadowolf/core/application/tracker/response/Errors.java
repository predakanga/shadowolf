package com.shadowolf.core.application.tracker.response;


/**
 * Enum wrapping common errors that occur several times.
 */
public enum Errors {
	/**
	 * Torrent is not present in the info_hash cache retrieved from MySQL.
	 */
	TORRENT_NOT_REGISTERED("Torrent is not registered with this tracker"),
	
	/**
	 * Passkey is not present in the passkey cache retrieved from MySQL.
	 */
	INVALID_PASSKEY("Unrecognized passkey"),
	
	/**
	 * Client some an unexpected event
	 */
	INVALID_EVENT("You sent an invalid event"),
	
	/**
	 * Client sent garbled nonsense instead of a valid info_hash.
	 */
	UNPARSEABLE_INFO_HASH("Failed to parse the info-hash your client passed."),
	
	/**
	 * Client is not present on the whitelist.
	 */
	BANNED_CLIENT("Your client is banned.  Please upgrade to a client on the whitelist."),
	
	/**
	 * Port is missing from announce.
	 */
	MISSING_PORT("Your client did not send required parameter: port"),
	
	/**
	 * info_hash is missing from announce.
	 */
	MISSING_INFO_HASH("Your client did not send required parameter: info_hash"),
	
	/**
	 * passkey is missing from announce.
	 */
	MISSING_PASSKEY("Your client did not send required parameter: passkey"),
	
	/**
	 * peer_id is missing from announce.
	 */
	MISSING_PEER_ID("Your client did not send required parameter: peer_id"),
	
	/**
	 * Client is seeding from too many locations; specified in config.xml.
	 */
	TOO_MANY_LOCATIONS("Seeding from too many locations"),
	
	/**
	 * Something went wrong with an IPv4 address; this should never actually appear in production.
	 */
	UNEXPECTED_4_PEER_LENGTH("IPv4 address: unexpected length"),
	
	/**
	 * Somehow, the IP sent to the servlet is invalid; this should never actually appear in production.
	 */
	INVALID_IP("Your IP appears to be invalid."),
	
	/**
	 * Something went wrong with an IPv6 address; this should never actually appear in production.
	 */
	UNEXPECTED_6_PEER_LENGTH("IPv6 address: unexpected length");
	
	private final String humanReadable;

	private Errors(final String humanReadable) {
		this.humanReadable = TrackerResponse.bencodeFailure(humanReadable);
	}

	public String toString() {
		return this.humanReadable;
	}
}