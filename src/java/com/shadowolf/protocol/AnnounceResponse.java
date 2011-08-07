package com.shadowolf.protocol;

/**
 * This isnt done!
 * @author Jon
 *
 */
public class AnnounceResponse {
	int seeders;
	int leechers;
	Peer[] peers;
	int interval;
	int minInterval;
	
	public AnnounceResponse(int seeders, int leechers, Peer[] peers, int interval, int minInterval) {
		super();
		this.seeders = seeders;
		this.leechers = leechers;
		this.peers = peers;
		this.interval = interval;
		this.minInterval = minInterval;
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
		return "";
	}
}
