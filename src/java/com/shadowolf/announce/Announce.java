package com.shadowolf.announce;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.tracker.TrackerResponse;
import com.shadowolf.tracker.TrackerRequest.Event;
import com.shadowolf.tracker.TrackerResponse.Errors;
import com.shadowolf.util.Data;


/**
 * Simple class that encapsulates and validates all of the data for a valid announce.
 *
 */
final public class Announce {
	
	private transient final Event event;
	
	private transient long uploaded;
	private transient long downloaded;
	private transient final long left;
	private transient final int numwant;
	
	private transient final String encoding;
	private transient final String infoHash;
	private transient final String peerId;
	/**
	 * This is stored as a String because the majority of requests
	 * don't need the numerical / byte value of the port.  It's may be
	 * slightly (a couple of bytes) less memory efficient to store as a 
	 * string, but the CPU time savings compensates.
	 */
	private transient final String port;
	
	/**
	 * @see port for justification why this is stored as a string.
	 */
	private transient final String IP; //NOPMD ... short var name
	private transient final String passkey;
	
	public Announce(final HttpServletRequest request) throws AnnounceException {
		//parseEvent() takes care of validating this field.
		this.event = this.parseEvent(request.getParameter("event"));
		
		//uploaded is mandatory, but we'll allow it missing to be interpreted as 0
		this.uploaded = this.parseLong(request.getParameter("uploaded"));
		
		//dealing with downloaded like we did uploaded:
		this.downloaded = this.parseLong(request.getParameter("downloaded"));
		
		//dealing with left the same way
		this.left = this.parseLong(request.getParameter("left"));
		
		//numwant is the same way
		this.numwant = this.parseInt(request.getParameter("numwant"));
		
		//get the encoding, which we need in a few places
		this.encoding = this.parseEncoding(request.getCharacterEncoding());
		
		//info_hash is mandatory
		this.infoHash = this.parseInfoHash(request.getParameter("info_hash"));
		
		//port is mandatory
		this.port = this.parseMandatoryString(request.getParameter("port"), Errors.MISSING_PORT);
		
		//IP is... impossible to not have
		this.IP = request.getRemoteAddr();

		//passkey is mandatory
		this.passkey = this.parseMandatoryString(request.getParameter("passkey"), Errors.MISSING_PASSKEY);
		
		//peer_id is mandatory
		this.peerId = this.parseMandatoryString(request.getParameter("peer_id"), Errors.MISSING_PEER_ID);
	}
	
	private String parseInfoHash(final String originalHash) throws AnnounceException{
		if(originalHash == null) {
			throw new AnnounceException(TrackerResponse.Errors.MISSING_INFO_HASH.toString());
		} else {
			String hexString;
			try {
				hexString =  Data.byteArrayToHexString(originalHash.getBytes(this.encoding));
			} catch(UnsupportedEncodingException e) {
				hexString = "";
			}
			
			return hexString;
		}
	}
	
	private String parseMandatoryString(final String param, final Errors error) throws AnnounceException {
		if(param == null) {
			throw new AnnounceException(error.toString());
		} else {
			return param;
		}
	}
	private String parseEncoding(final String enc) {
		String encoding;
		
		if(enc == null) {
			encoding = "ISO-8859-1";
		} else {
			encoding = enc;
		}
		
		return encoding;
	}
	
	private long parseLong(final String stringVal) {
		long longVal;

		if(stringVal == null) {
			longVal = 0;
		} else {
			longVal = Long.parseLong(stringVal);
		}
		
		return longVal;
	}
	

	private int parseInt(final String stringVal) {
		int intVal;

		if(stringVal == null) {
			intVal = 0;
		} else {
			intVal = Integer.parseInt(stringVal);
		}
		
		return intVal;
	}
	private Event parseEvent(final String paramEvent) {
		Event event;
		if("completed".equals(paramEvent)) {
			event = Event.COMPLETED;
		} else if("stopped".equals(paramEvent)) {
			event = Event.STOPPED;
		} else if("started".equals(paramEvent)) {
			event = Event.STARTED;
		} else {
			event = Event.ANNOUNCE;
		}
		
		return event;
	}

	public Event getEvent() {
		return event;
	}

	public long getUploaded() {
		return uploaded;
	}

	public long getDownloaded() {
		return downloaded;
	}

	public long getLeft() {
		return left;
	}

	public int getNumwant() {
		return numwant;
	}

	public String getInfoHash() {
		return infoHash;
	}

	public String getPeerId() {
		return peerId;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getPort() {
		return port;
	}

	public String getIP() {
		return IP;
	}

	public String getPasskey() {
		return passkey;
	}

	public void setUploaded(final long uploaded) {
		this.uploaded = uploaded;
	}

	public void setDownloaded(final long downloaded) {
		this.downloaded = downloaded;
	}
	
}
