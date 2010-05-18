package com.shadowolf.core.application.tracker;

import java.util.Arrays;

/**
 * Simple class used to represent a client. A client, in the SW sense, is a
 * particular "client" on an IP and port using a particular passkey. This does
 * mean that n clients can exist on the same IP and port.
 */
public class ClientIdentifier {
	private final byte[] ipAddress;
	private final String passkey;
	private final byte[] port;

	/**
	 * Constructs a new ClientIdentifier with the given IP, port and passkey.
	 */
	public ClientIdentifier(final byte[] ipAddress, final byte[] port, final String passkey) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.passkey = passkey;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		
		final ClientIdentifier other = (ClientIdentifier) obj;
		
		if (!Arrays.equals(this.ipAddress, other.ipAddress)) {
			return false;
		}
		if (this.passkey == null) {
			if (other.passkey != null) {
				return false;
			}
		} else if (!this.passkey.equals(other.passkey)) {
			return false;
		}
		if (!Arrays.equals(this.port, other.port)) {
			return false;
		}
		return true;
	}

	/**
	 * Return this client's IP address.
	 */
	public byte[] getIpAddress() {
		return this.ipAddress;
	}

	/**
	 * Return this client's passkey.
	 */
	public String getPasskey() {
		return this.passkey;
	}

	/**
	 * Return this client's port.
	 */
	public byte[] getPort() {
		return this.port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.ipAddress);
		result = prime * result + ((this.passkey == null) ? 0 : this.passkey.hashCode());
		result = prime * result + Arrays.hashCode(this.port);
		return result;
	}

}