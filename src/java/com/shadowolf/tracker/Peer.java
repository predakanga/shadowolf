package com.shadowolf.tracker;

import java.net.UnknownHostException;
import java.util.Date;

//import org.apache.log4j.Logger;

//PMD 0 errors
public class Peer {
	//private static final Logger LOGGER = Logger.getLogger(Peer.class);
	private long lastAnnounce;
	final private long uploaded;
	final private long downloaded;
	final private byte[] ipAddress;
	final private byte[] port;
	
	public Peer(final long uploaded,final long downloaded, final String ipAddress, final String port) throws UnknownHostException  {
		this.uploaded = uploaded;
		this.downloaded = downloaded;
		this.lastAnnounce = new Date().getTime();
		this.ipAddress = TrackerRequest.IPToBytes(ipAddress);
		this.port = TrackerRequest.portToBytes(port);
		
		/*LOGGER.debug("Constructing peer with parameters:" + "\n" +
				"\t\t" + "passkey: " + this.passkey + "\n" +
				"\t\t" + "peer_id: " + this.peerId + "\n" +
				"\t\t" + "info_hash: " + this.infoHash + "\n" +
				"\t\t" + "uploaded: " + this.uploaded + "\n" +
				"\t\t" + "downloaded: " + this.downloaded + "\n" +
				"\t\t" + "lastAnnounce: " + this.lastAnnounce.toString() + "\n" +
				"\t\t" + "IP: " + this.ipAddress +  "\n" +
				"\t\t" + "Port: " + this.port);
				
		 */
	}
	
	public long getLastAnnounce() {
		return this.lastAnnounce;
	}
	public void setLastAnnounce(final Date lastAnnounce) {
		this.lastAnnounce = (int)lastAnnounce.getTime();
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
}