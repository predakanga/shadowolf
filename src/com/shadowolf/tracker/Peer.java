package com.shadowolf.tracker;

import java.io.Serializable;
import java.util.Date;

public class Peer implements Serializable{
	private static final long serialVersionUID = 1L;
	private String port;
	private String passkey;
	private String ipAddress;
	private Date lastAnnounce;
	
	public String getPort() {
		return this.port;
	}
	public void setPort(final String port) {
		this.port = port;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(final String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public Date getLastAnnounce() {
		return this.lastAnnounce;
	}
	public void setLastAnnounce(final Date lastAnnounce) {
		this.lastAnnounce = lastAnnounce;
	}

	public String getPasskey() {
		return passkey;
	}
	public void setPasskey(String passkey) {
		this.passkey = passkey;
	}
}
