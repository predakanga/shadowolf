package com.shadowolf.protocol;

import com.google.common.base.Charsets;

/**
 * This isnt done!
 * @author Jon
 *
 */
public class AnnounceResponse {
	private int seeders;
	private int leechers;
	private Peer[] peers;
	private int interval;
	private int minInterval;
	private String error;
	
	public AnnounceResponse(int seeders, int leechers, Peer[] peers, int interval, int minInterval) {
		super();
		this.seeders = seeders;
		this.leechers = leechers;
		this.peers = peers;
		this.interval = interval;
		this.minInterval = minInterval;
		this.error = "";
	}
	
	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * If this is set, all other data in the announce will be
	 * discarded, and the error message will be sent to the client. 
	 * <strong>The empty string is not a valid error and will be 
	 * treated as if no error exists!</strong>
	 * @param error the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * @return the seeders
	 */
	public int getSeeders() {
		return seeders;
	}
	
	/**
	 * @param seeders the seeders to set
	 */
	public void setSeeders(int seeders) {
		this.seeders = seeders;
	}
	
	/**
	 * @return the leechers
	 */
	public int getLeechers() {
		return leechers;
	}
	
	/**
	 * @param leechers the leechers to set
	 */
	public void setLeechers(int leechers) {
		this.leechers = leechers;
	}
	
	/**
	 * @return the peers
	 */
	public Peer[] getPeers() {
		return peers;
	}
	
	/**
	 * @param peers the peers to set
	 */
	public void setPeers(Peer[] peers) {
		this.peers = peers;
	}
	
	/**
	 * @return the interval
	 */
	public int getInterval() {
		return interval;
	}
	
	/**
	 * @param interval the interval to set
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	/**
	 * @return the minInterval
	 */
	public int getMinInterval() {
		return minInterval;
	}
	
	/**
	 * @param minInterval the minInterval to set
	 */
	public void setMinInterval(int minInterval) {
		this.minInterval = minInterval;
	}
	
	public String bencode() {
		if(error.length() > 0) {
			return "d14:failure reason"+error.length()+":"+error+"e\r\n";
		}
		StringBuilder sb = new StringBuilder();
		
		String s = "d8:intervali" + interval + "e" + "12:min intervali" + minInterval + "e10:incompletei" + leechers + "e8:completei" + seeders + "e";
		sb.append(s.getBytes(Charsets.UTF_8));
		sb.append(encodePeers());
		sb.append("e\r\n".getBytes(Charsets.UTF_8));
		
		return sb.toString();
	}
	
	private byte[] encodePeers() {
		//This is required to know how big of an array to allocate.
		int ipv4Count = 0;
		int ipv6Count = 0;
		
		for(Peer p : peers) {
			if(p.getAddress().getAddress().getAddress().length > 4) {
				ipv6Count++;
			} else {
				ipv4Count++;
			}
		}
		
		byte[] ipv4start = ("5:peers"+(ipv4Count*6)+":").getBytes(Charsets.UTF_8);
		byte[] ipv6start = new byte[0];
		
		if(ipv6Count > 0) {
			ipv6start = ("6:peers6"+(ipv6Count*18)+":").getBytes(Charsets.UTF_8);
		}
		
		int size = ipv6start.length + ipv4start.length + (ipv6Count * 18) + (ipv4Count * 6);
		
		byte[] result = new byte[size];
		
		int ipv4Index = 0;
		int ipv6Index = ipv4start.length+(6*ipv4Count);
		
		System.arraycopy(ipv4start, 0, result, ipv4Index, ipv4start.length);
		System.arraycopy(ipv6start, 0, result, ipv6Index, ipv6start.length);
		
		ipv4Index += ipv4start.length;
		ipv6Index += ipv6start.length;
		
		for(Peer p : peers) {
			int index;
			byte[] b = p.getAddress().getAddress().getAddress();
			
			if(b.length > 4) {
				index = ipv6Index; 
				ipv4Index += 6;
			} else {
				index = ipv6Index;
				ipv6Index += 18;
			}

			System.arraycopy(b, 0, result, index, b.length);
		}
		
		return result;
	}
	
}
