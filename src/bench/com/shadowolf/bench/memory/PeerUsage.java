package com.shadowolf.bench.memory;

import java.net.UnknownHostException;

import com.shadowolf.tracker.AnnounceException;
import com.shadowolf.user.User;
import com.shadowolf.user.UserFactory;

public class PeerUsage {
	public static void main(String[] args) throws UnknownHostException, IllegalAccessException, AnnounceException {
		final int maxPeers = 1000000;
		final byte[] infoHash = new byte[] { (byte)0xFF, (byte)0xFF };
		
		for(int i=0; i < maxPeers; i++) {
			User u = UserFactory.getUser("foo", i + "");
			u.getPeer(infoHash, "127.0.0.1", "6500");
			if(i % 100 == 0) {
				System.out.println("At " + i + " peers: " + Runtime.getRuntime().freeMemory() +
						" of " + Runtime.getRuntime().totalMemory());
			}
		}
	}
}
