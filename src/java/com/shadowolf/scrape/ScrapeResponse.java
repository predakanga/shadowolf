package com.shadowolf.scrape;

import java.io.UnsupportedEncodingException;

import com.shadowolf.tracker.Errors;
import com.shadowolf.tracker.ScrapeException;
import com.shadowolf.tracker.TrackerResponse;
import com.shadowolf.user.PeerList;
import com.shadowolf.user.PeerListFactory;
import com.shadowolf.util.Data;

final public class ScrapeResponse {
	private final String infoHash; //NOPMD
	private final String responseString; //NOPMD
	private final long fetchTime; //NOPMD
	
	public ScrapeResponse(final String infoHash, final String encoding) throws ScrapeException {
		this.infoHash = infoHash;
		
		try {
			final byte[] cleanHashB = infoHash.getBytes(encoding);
			final String cleanHash = Data.byteArrayToHexString(cleanHashB);
			
			StringBuilder builder = new StringBuilder();
			final PeerList peerlist = PeerListFactory.getList(cleanHash);
			final int timesCompleted; //NOPMD - well, no
			final Integer timesCompletedObj = ScrapeServlet.completedTotals.get(infoHash);
			if (timesCompletedObj == null) {
				timesCompleted = 0;
			} else {
				timesCompleted = timesCompletedObj.intValue();
			}
			builder = TrackerResponse.bencodedScrape(peerlist.getSeederCount(), peerlist.getLeecherCount(), timesCompleted, infoHash, builder);
			this.responseString = builder.toString();
			this.fetchTime = System.nanoTime();
		} catch (UnsupportedEncodingException e) {
			throw new ScrapeException(Errors.UNPARSEABLE_INFO_HASH, e);
		}
	}

	public String getInfoHash() {
		return infoHash;
	}

	public String getResponseString() {
		return responseString;
	}
	
	public long getFetchTime() {
		return this.fetchTime;
	}

	public boolean isExpired() {
		return this.fetchTime + 600000000000.0 < System.nanoTime();
	}
}
