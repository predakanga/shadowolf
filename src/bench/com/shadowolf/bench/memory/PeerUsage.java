package com.shadowolf.bench.memory;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;


import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.user.User;
import com.shadowolf.user.UserFactory;

public class PeerUsage {
	public static void main(String[] args) throws UnknownHostException, IllegalAccessException, AnnounceException, UnsupportedEncodingException {
		final int maxPeers = 10;
		final int maxUsers = 10000000;
		
		//
		
		for(int i=0; i < maxUsers; i++) {
			final User u = UserFactory.getUser("0123456789001234567890", i + "");
			
			for(int n = 0; n < maxPeers; n++) {
				//twenty bytes for ole infoHash
				final byte[] infoHash = new byte[] { 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF, 
					(byte)0xFF,
					(byte)n
				};
				u.getPeer(infoHash, "127.0.0.1", "6500");
			}
			
			if(i % 1000 == 0) {
				System.out.println("At " + i + " peers: " + Runtime.getRuntime().freeMemory() +
						" of " + Runtime.getRuntime().totalMemory());
			}
		}
	}
}
