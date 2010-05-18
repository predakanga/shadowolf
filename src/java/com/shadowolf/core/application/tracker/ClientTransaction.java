package com.shadowolf.core.application.tracker;

public interface ClientTransaction {
	public byte[] getPort();
	public byte[] getIP();
	public byte[] getInfoHash();
	public String getPasskey();
	public ClientIdentifier getClientIdentifier();
}