package com.shadowolf.scrape;

import java.util.concurrent.ConcurrentHashMap;

import com.shadowolf.tracker.Errors;
import com.shadowolf.tracker.ScrapeException;
import com.shadowolf.tracker.TrackerResponse;

final public class ScrapeResponseFactory {
	private static ConcurrentHashMap<String, ScrapeResponse> cache = new ConcurrentHashMap<String, ScrapeResponse>();
	
	private ScrapeResponseFactory() {
		
	}
	
	public static String scrape(final String[] parameterValues) throws ScrapeException {
		if (parameterValues == null) {
			throw new ScrapeException(Errors.MISSING_INFO_HASH.toString());
		} else {
			StringBuilder builder = new StringBuilder();
			
			final String[] infohashes = parameterValues.clone();
			
			for (String infohash : infohashes) {
				final ScrapeResponse scrapeResponse = ScrapeResponseFactory.getScrapeResponse(infohash);
				builder.append(scrapeResponse.getResponseString());
			}
			
			builder = TrackerResponse.wrapInDict(builder, "files");
			
			return builder.toString();
		}
	}
	
	public static void remove(final String infohash) {
		cache.remove(infohash);
	}

	public static ScrapeResponse getScrapeResponse(final String infohash) {
		ScrapeResponse scrape = cache.get(infohash);
		
		if (scrape == null || scrape.isExpired()) {
			if (scrape != null && scrape.isExpired()) {
				cache.remove(infohash);
			}
			
			scrape = new ScrapeResponse(infohash);
			
			if (cache.get(infohash) == null) {
				cache.put(infohash, scrape);
			}
		}
		
		return scrape;
	}
}