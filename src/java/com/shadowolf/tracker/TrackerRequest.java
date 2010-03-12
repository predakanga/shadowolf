package com.shadowolf.tracker;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

public class TrackerRequest {
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
	
	public static byte[] portToBytes(String port) {
		int portI = Integer.parseInt(port);
		return new byte[] {
				(byte)(((byte)portI >>> 8) & 0xFF),
				(byte)((byte)portI & 0xFF),
		};
	}
	
	public static byte[] IPToBytes(String IP) throws UnknownHostException {
		if(isIPv6(IP)) {
			return Inet6Address.getByName(IP).getAddress();
		} else {
			return Inet4Address.getByName(IP).getAddress();
		}
	}
	
	public final static boolean isIPv6(final String IP) {
    	return IP != null && IP.contains(":");
    }
	
}
