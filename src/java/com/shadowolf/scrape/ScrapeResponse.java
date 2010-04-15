package com.shadowolf.scrape;

import com.shadowolf.tracker.TrackerResponse;
import com.shadowolf.user.PeerList;
import com.shadowolf.user.PeerListFactory;

final public class ScrapeResponse {
	private final String infoHash; //NOPMD
	private final String responseString; //NOPMD
	private final long fetchTime; //NOPMD
	
	public ScrapeResponse(final String infoHash, final String responseString) {
		this.infoHash = infoHash;
		this.responseString = responseString;
		this.fetchTime = System.nanoTime();
	}
	
	public ScrapeResponse(final String infoHash) {
		this.infoHash = infoHash;
		StringBuilder builder = new StringBuilder();
		final PeerList peerlist = PeerListFactory.getList(infoHash);
		final int timesCompleted;
		final Integer timesCompletedObj = ScrapeServlet.completedTotals.get(infoHash);
		if (timesCompletedObj == null) {
			timesCompleted = 0;
		} else {
			timesCompleted = timesCompletedObj.intValue();
		}
		builder = TrackerResponse.bencodedScrape(peerlist.getSeederCount(), peerlist.getLeecherCount(), timesCompleted, infoHash, builder);
		this.responseString = builder.toString();
		this.fetchTime = System.nanoTime();
	}

	public String getInfoHash() {
		return infoHash;
	}

	public String getResponseString() {
		return responseString;
	}

	public long getFetchTime() {
		return fetchTime;
	}

	public boolean isExpired() {
		return this.fetchTime + 600000000000.0 < System.nanoTime();
	}
}
