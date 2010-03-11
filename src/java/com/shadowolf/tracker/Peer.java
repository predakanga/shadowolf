package com.shadowolf.tracker;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

//PMD 0 errors
public class Peer implements Serializable {
	private static final Logger LOGGER = Logger.getLogger(Peer.class);
	private static final long serialVersionUID = 1L;
	private String peerId;
	private String passkey;
	private long lastAnnounce;
	private long infoHash;
	private long uploaded;
	private long downloaded;
	private String ipAddress;
	private String port;
	
	
	public Peer() {
		//This exists for javabean compliance	
	}
	
	public Peer(final String passkey, final String peerId, final long infoHash, 
			final long uploaded, final long downloaded, final String ipAddress, final String port) {
		this.passkey = passkey;
		this.peerId = peerId;
		this.infoHash = infoHash;
		this.uploaded = uploaded;
		this.downloaded = downloaded;
		this.lastAnnounce = new Date().getTime();
		this.ipAddress = ipAddress;
		this.port = port;
		
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
	
	public Peer(final String passkey, final String peerId, final long infoHash,
			final String ipAddress, final String port) {
		this(passkey, peerId, infoHash, 0, 0, ipAddress, port);
	}
	
	public long getLastAnnounce() {
		return this.lastAnnounce;
	}
	public void setLastAnnounce(final Date lastAnnounce) {
		this.lastAnnounce = lastAnnounce.getTime();
	}

	public String getPasskey() {
		return passkey;
	}
	public void setPasskey(final String passkey) {
		this.passkey = passkey;
	}
	public long getInfoHash() {
		return infoHash;
	}
	public void setInfoHash(final long infoHash) {
		this.infoHash = infoHash;
	}
	public void setUploaded(final long uploaded) {
		this.uploaded = uploaded;
	}
	public long getUploaded() {
		return uploaded;
	}
	public void setDownloaded(final long downloaded) {
		this.downloaded = downloaded;
	}
	public long getDownloaded() {
		return downloaded;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public String getPeerId() {
		return this.peerId;
	}

	public String getIpAddress() {
		return this.ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getPort() {
		return this.port;
	}

	public void setPort(String port) {
		this.port = port;
	}
}