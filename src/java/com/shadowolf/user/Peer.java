package com.shadowolf.user;

import java.net.UnknownHostException;
import java.util.Date;

import com.shadowolf.tracker.TrackerRequest;

//import org.apache.log4j.Logger;

/**
 * Data class that encapsulates the data for a peer.
 */
public class Peer {
	//private static final Logger LOGGER = Logger.getLogger(Peer.class);
	private long lastAnnounce;
	private long uploaded; //NOPMD
	private long downloaded; //NOPMD
	final private byte[] ipAddress; //NOPMD
	final private byte[] port; //NOPMD
	
	public Peer(final long uploaded,final long downloaded, final String ipAddress, 
			final String port) throws UnknownHostException  {

		this.uploaded = uploaded;
		this.downloaded = downloaded;
		this.lastAnnounce = new Date().getTime();
		this.ipAddress = TrackerRequest.IPToBytes(ipAddress);
		this.port = TrackerRequest.portToBytes(port);
	}
	
	public long getLastAnnounce() {
		return this.lastAnnounce;
	}
	public void setLastAnnounce(final Date lastAnnounce) {
		this.lastAnnounce = lastAnnounce.getTime();
	}

	public long getUploaded() {
		return uploaded;
	}
	public long getDownloaded() {
		return downloaded;
	}
	public byte[] getIpAddress() {
		return this.ipAddress;
	}
	public byte[] getPort() {
		return this.port;
	}

	public void setUploaded(long uploaded) {
		this.uploaded = uploaded;
	}

	public void setDownloaded(long downloaded) {
		this.downloaded = downloaded;
	}
}