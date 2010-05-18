package com.shadowolf.core.application.announce;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import com.shadowolf.core.application.tracker.ClientIdentifier;
import com.shadowolf.core.application.tracker.ClientTransaction;
import com.shadowolf.core.application.tracker.response.Errors;

/**
 *	Simple bean-like class that encapsulates, after validating, 
 *	all the data for a valid announce. 
 */
public final class Announce implements Serializable, ClientTransaction {
	private static final long serialVersionUID = -5364759414320537204L;
	
	//this is hardcoded because there's not really any justifiable reason to change it.
	private static final int DEFAULT_NUMWANT = 200;
	
	private final Events event;
	
	private final int numwant;
	private long uploaded;
	private long downloaded;
	private long left;
	
	private final byte[] port;
	private final byte[] IP;
	private final byte[] infoHash;
	
	private final String passkey;
	private final String peerId;
	private final ClientIdentifier clientIdentifier;
	
	/**
	 * Creates a new announce object, validating parameters.
	 * @param request the request received by the servlet, logic for parsing fields, etc. is encapsulated here
	 * @throws AnnounceException if something goes wrong parsing fields, fields are not presented, etc. 
	 * 	this will get thrown, allowing the servlets to jump immediately to error.
	 */
	public Announce(final HttpServletRequest request) throws AnnounceException {
		this.event = this.parseEvent(request.getParameter("event"));
		
		this.numwant = this.parseInt(request.getParameter("numwant")) > 0 
						? this.parseInt(request.getParameter("numwant")) 
						: DEFAULT_NUMWANT;
		this.uploaded = this.parseLong(request.getParameter("uploaded"));
		this.downloaded = this.parseLong(request.getParameter("downloaded"));
		this.left = this.parseLong(request.getParameter("left"));
		
		this.port = this.parsePort(request.getParameter("port"));
		this.IP = this.parseIPAddress(request.getRemoteAddr());
		this.infoHash = this.parseInfoHash(request.getParameter("info_hash"));
		
		this.passkey = this.parsePasskey((String)request.getAttribute("passkey"));
		this.clientIdentifier =  new ClientIdentifier(this.IP, this.port, this.passkey);
		this.peerId = this.parsePeerId(request.getParameter("peer_id"));
	}
	
	private String parsePeerId(String peerId) throws AnnounceException {
		if(peerId == null) {
			throw new AnnounceException(Errors.MISSING_PEER_ID);
		}
		
		return peerId;
	}
	
	private byte[] parseInfoHash(final String originalHash) throws AnnounceException {
		if (originalHash == null) {
			throw new AnnounceException(Errors.MISSING_INFO_HASH);
		} else {
			try {
				byte[] hash = originalHash.getBytes("ISO-8859-1");
				if(hash.length != 20) {
					throw new AnnounceException(Errors.UNPARSEABLE_INFO_HASH);
				}
				
				return hash;
			} catch (final UnsupportedEncodingException e) {
				throw new AnnounceException(Errors.UNPARSEABLE_INFO_HASH, e);
			}
		}
	}
	
	private String parsePasskey(String passkey) throws AnnounceException {
		if(passkey == null) {
			throw new AnnounceException(Errors.MISSING_PASSKEY);
		}
		
		return passkey;
	}
	
	private Events parseEvent(String event) throws AnnounceException {
		if(event == null || "".equals(event)) {
			return Events.ANNOUNCE;
		} else if("completed".equals(event)) {
			return Events.COMPLETED;
		} else if("started".equals(event)) {
			return Events.STARTED;
		} else if("stopped".equals(event)) {
			return Events.STOPPED;
		} else {
			throw new AnnounceException(Errors.INVALID_EVENT);
		}
	}
	
	private byte[] parsePort(final String port) throws AnnounceException {
		if(port == null) {
			throw new AnnounceException(Errors.MISSING_PORT);
		}
		
		final int portI = Integer.parseInt(port);
		return new byte[] {
				(byte)(((byte)portI >>> 8) & 0xFF),
				(byte)((byte)portI & 0xFF),
		};
	}

	private byte[] parseIPAddress(final String host) throws AnnounceException {
		try {
			return InetAddress.getByName(host).getAddress();
		} catch (final UnknownHostException e) {
			throw new AnnounceException(Errors.INVALID_IP);
		}
	}
	
	private int parseInt(final String stringVal) {
		int intVal;

		if (stringVal == null) {
			intVal = 0;
		} else {
			intVal = Integer.parseInt(stringVal);
		}

		return intVal;
	}
	
	private long parseLong(final String stringVal) {
		long longVal;

		if (stringVal == null) {
			longVal = 0;
		} else {
			longVal = Long.parseLong(stringVal);
		}

		return longVal;
	}
	
	/**
	 * Get the event specified for this announce.
	 * @return the event
	 * @see com.shadowolf.tracker.TrackerRequest.Event
	 */
	public Events getEvent() {
		return this.event;
	}

	/**
	 * Get the uploaded amount for this announce, in bytes.
	 * 
	 * Note that the uploaded amount is total for the entire session, not simply since the last announce.
	 * @return the uploaded amount.
	 */
	public long getUploaded() {
		return this.uploaded;
	}

	/**
	 * Get the downloaded amount for this announce, in bytes.
	 * 
	 * Note that the downloaded amount is total for the entire session, not simply since the last announce.
	 * @return the downloaded amount.
	 */
	public long getDownloaded() {
		return this.downloaded;
	}

	/**
	 * Get the left amount for this announce, in bytes.
	 * 
	 * The left amount is sent on every announce as a total sum of what isn't downloaded on the torrent.
	 * A value greater than 0 can indicate either a leecher or a partial seeder.
	 * @return the left amount.
	 */
	public long getLeft() {
		return this.left;
	}

	/**
	 * Get the numwant amount for this announce.
	 * 
	 * The numwant is sent, optionally, to tell the tracker how many peers to send back.  If not set, defaults to 200.
	 * @return the amount of peers requested.
	 */
	public int getNumwant() {
		return this.numwant;
	}

	/**
	 * Get the info_hash for this torrent.
	 * 
	 * According to the protocol, info_hashes are 20-byte sha1 hashes that are sent "raw," for simplification of
	 * debugging and value comparisons, this is translated to a 40-character hexadecimal string.
	 * @return the hex-encoded info_hash string
	 */
	public byte[] getInfoHash() {
		return this.infoHash;
	}

	/**
	 * Get the peer_id for this torrent.
	 * 
	 * The peer_id is a partially-unique field sent with each announce.  The first few characters are usually a client
	 * identifier, followed by a random string used to uniquely identify this tracker session on a per torrent basis.  The bytes are usually not
	 * characters specified by a character set and if they are, it is entirely random.  As such, this string gets interpreted
	 * using the character set (see {@link #getEncoding()}) associated with this announce.
	 * @return the peer_id
	 * @see <a href="http://www.bittorrent.org/beps/bep_0020.html">Bep 0020 - the specification for peer_id conventions</a>
	 */
	public String getPeerId() {
		return this.peerId;
	}

	/**
	 * Get the port that the client is listening on, as specified in the announce.
	 * @return the port the client is listening on.
	 */
	public byte[] getPort() {
		return this.port;
	}

	/**
	 * Get the IP the client is running on.  The "IP" GET parameter is ignored and this is set to the address the announce originated from.
	 * This is by design and is not a bug.
	 * @return the client's IP
	 */
	public byte[] getIP() {
		return this.IP;
	}

	/**
	 * Get the passkey associated with this announce.
	 * @return the client's passkey
	 */
	public String getPasskey() {
		return this.passkey;
	}

	/**
	 * Sets the upload to the specified amount.  The original amount is overwritten, not added.
	 * 
	 * THIS SHOULD BE USED WITH CAUTION.
	 * @param uploaded the new amount of upload
	 */
	public void setUploaded(final long uploaded) {
		this.uploaded = uploaded;
	}

	/**
	 * Sets the download to the specified amount.  The original amount is overwritten, not added.
	 * 
	 * THIS SHOULD BE USED WITH CAUTION.
	 * @param downloaded the new amount of download
	 */
	public void setDownloaded(final long downloaded) {
		this.downloaded = downloaded;
	}

	/**
	 * Get the client identifier; used to identify a specific client.
	 * <br/>
	 * While this isn't perfect, there isn't a more reliable way to uniquely identify a client and
	 * cases where it is wrong are likely to never actually exist.
	 */
	public ClientIdentifier getClientIdentifier() {
		return clientIdentifier;
	}
}
