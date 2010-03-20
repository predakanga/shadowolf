package com.shadowolf.tracker;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

final public class TrackerRequest {
	private TrackerRequest() {
		
	}
	public enum Event {
		STARTED {
			public String toString() {
				return "started";
			}
		},
		STOPPED { 
			public String toString() {
				return "stopped";
			}
		},
		ANNOUNCE {
			public String toString() {
				return "announce";
			}
		},
		COMPLETED {
			public String toString() {
				return "completed";
			}
		}
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
			return Inet6Address.getByName(ipAddy).getAddress(); //NOPMD
		} else {
			return Inet4Address.getByName(ipAddy).getAddress();
		}
	}
	
	public static boolean isIPv6(final String ipAddy) {
    	return ipAddy != null && ipAddy.contains(":");
    }
	
}
