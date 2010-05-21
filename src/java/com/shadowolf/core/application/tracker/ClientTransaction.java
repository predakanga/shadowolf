package com.shadowolf.core.application.tracker;

/**
 * Interface that represents a client-tracker transaction.  Implemented by Announce and Scrape.
 */
public interface ClientTransaction {
	/**
	 * Get the port from the client.  It's a mandatory field and implementors should enforce this.
	 * Encoded as a 2-size byte array, as it would be on a network interface.
	 */
	public byte[] getPort();
	/**
	 * Gets the IP from the client.  It's a byte-array (network interface style) of size either
	 * 4 or 16, depending whether the client is IPv4 or IPv6, respectively.  Implementations
	 * must ensure the size of this array.
	 */
	public byte[] getIP();
	/**
	 * Get the info_hash for the torrent that is being acted upon.  It's a mandatory field and
	 * implementors should enforce this.
	 */
	public byte[] getInfoHash();
	/**
	 * Get the passkey, used to uniquely identify a user in the database.  Also a mandatory
	 * field that implementations should guarantee. 
	 */
	public String getPasskey();
	/**
	 * Provide a {@link ClientIdentifier} instance that represents the client in this transaction.
	 */
	public ClientIdentifier getClientIdentifier();
}