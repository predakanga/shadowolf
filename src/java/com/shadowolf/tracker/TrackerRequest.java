package com.shadowolf.tracker;

import java.net.InetAddress;
import java.net.UnknownHostException;

final public class TrackerRequest {
	private TrackerRequest() {

	}


	public static byte[] portToBytes(final String port) {
		final int portI = Integer.parseInt(port);
		return new byte[] {
				(byte)(((byte)portI >>> 8) & 0xFF),
				(byte)((byte)portI & 0xFF),
		};
	}

	public static byte[] IPToBytes(final String ipAddy) throws UnknownHostException { //NOPMD ... IP is fine!
		if(isIPv6(ipAddy)) {
			return InetAddress.getByName(ipAddy).getAddress(); //NOPMD
		} else {
			return InetAddress.getByName(ipAddy).getAddress();
		}
	}

	public static boolean isIPv6(final String ipAddy) {
		return (ipAddy != null) && ipAddy.contains(":");
	}

}
