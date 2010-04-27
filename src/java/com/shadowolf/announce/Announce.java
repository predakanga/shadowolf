package com.shadowolf.announce;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.Errors;
import com.shadowolf.util.Data;

/**
 * Simple class that encapsulates and validates all of the data for a valid
 * announce.
 * 
 */
final public class Announce {
	//private final Logger LOGGER = Logger.getLogger(Announce.class);
	
	public static final int DEFAULT_NUMWANT = 200;

	private final Event event;

	private long uploaded;
	private long downloaded;
	private final long left;
	private final int numwant;

	private final String key;
	private final String infoHash;
	private final String peerId;
	/**
	 * This is stored as a String because the majority of requests don't need
	 * the numerical / byte value of the port. It's may be slightly (a couple of
	 * bytes) less memory efficient to store as a string, but the CPU time
	 * savings compensates.
	 */
	private transient final String port;

	/**
	 * @see port for justification why this is stored as a string.
	 */
	private transient final String IP; // NOPMD ... short var name
	private transient final String passkey;

	/**
	 * Constructor, takes an HttpServletRequest instance and parses the required
	 * fields and validates them
	 * 
	 * @param request the
	 * 			  <a href="http://java.sun.com/products/servlet/2.5/docs/servlet-2_5-mr2/javax/servlet/http/HttpServletRequest.html">
	 *            HttpServletRequest<a> instance to parse
	 * @throws AnnounceException
	 *             thrown if any validation fails.
	 */
	public Announce(final HttpServletRequest request) throws AnnounceException {
		// parseEvent() takes care of validating this field.
		this.event = this.parseEvent(request.getParameter("event"));

		// uploaded is mandatory, but we'll allow it missing to be interpreted
		// as 0
		this.uploaded = this.parseLong(request.getParameter("uploaded"));

		// dealing with downloaded like we did uploaded:
		this.downloaded = this.parseLong(request.getParameter("downloaded"));

		// dealing with left the same way
		this.left = this.parseLong(request.getParameter("left"));

		// numwant is the same way
		this.numwant = this.parseInt(request.getParameter("numwant")) > 0 ? this.parseInt(request.getParameter("numwant")) : DEFAULT_NUMWANT;

		// info_hash is mandatory
		this.infoHash = this.parseInfoHash(request.getParameter("info_hash"));

		// port is mandatory
		this.port = this.parseMandatoryString(request.getParameter("port"), Errors.MISSING_PORT);

		// IP is... impossible to not have
		this.IP = request.getRemoteAddr();

		//key is a client ID
		this.key = request.getParameter("key") == null 
					? this.IP + this.port
					: request.getParameter("key");
		
		// passkey is mandatory
		//this.passkey = this.parseMandatoryString(request.getParameter("passkey"), Errors.MISSING_PASSKEY);
		this.passkey = this.parseMandatoryObjectAsString(request.getAttribute("passkey"), Errors.MISSING_PASSKEY);

		// peer_id is mandatory
		this.peerId = this.parseMandatoryString(request.getParameter("peer_id"), Errors.MISSING_PEER_ID);
	}
	
	private String parseInfoHash(final String originalHash) throws AnnounceException {
		if (originalHash == null) {
			throw new AnnounceException(Errors.MISSING_INFO_HASH);
		} else {
			try {
				return Data.byteArrayToHexString(originalHash.getBytes("ISO-8859-1"));
			} catch (final UnsupportedEncodingException e) {
				throw new AnnounceException(Errors.UNPARSEABLE_INFO_HASH, e);
			}
		}
	}

	private String parseMandatoryString(final String param, final Errors error) throws AnnounceException {
		if (param == null) {
			throw new AnnounceException(error);
		} else {
			return param;
		}
	}
	
	private String parseMandatoryObjectAsString(final Object param, final Errors error) throws AnnounceException {
		if(param instanceof String) {
			return (String) param;
		}
		else {
			throw new AnnounceException(error);
		}
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

	private int parseInt(final String stringVal) {
		int intVal;

		if (stringVal == null) {
			intVal = 0;
		} else {
			intVal = Integer.parseInt(stringVal);
		}

		return intVal;
	}

	private Event parseEvent(final String paramEvent) {
		Event event;
		if ("completed".equals(paramEvent)) {
			event = Event.COMPLETED;
		} else if ("stopped".equals(paramEvent)) {
			event = Event.STOPPED;
		} else if ("started".equals(paramEvent)) {
			event = Event.STARTED;
		} else {
			event = Event.ANNOUNCE;
		}

		return event;
	}

	
	/**
	 * Get the event specified for this announce.
	 * @return the event
	 * @see com.shadowolf.tracker.TrackerRequest.Event
	 */
	public Event getEvent() {
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
	public String getInfoHash() {
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
	 * Gets the key that is intended to represent a particular client.  Sets to "IP + port" if not present, as it is an optional field.
	 * @return the key, or IP + port if not present.
	 */
	public String getKey() {
		return this.key;
	}
	/**
	 * Get the port that the client is listening on, as specified in the announce.
	 * @return the port the client is listening on.
	 */
	public String getPort() {
		return this.port;
	}

	/**
	 * Get the IP the client is running on.  The "IP" GET parameter is ignored and this is set to the address the announce originated from.
	 * This is by design and is not a bug.
	 * @return the client's IP
	 */
	public String getIP() {
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

}
