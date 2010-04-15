package com.shadowolf.scrape;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;

import com.shadowolf.tracker.Errors;
import com.shadowolf.tracker.ScrapeException;
import com.shadowolf.tracker.TrackerResponse;
import com.shadowolf.util.Data;

final public class ScrapeResponseFactory {
	private static ConcurrentHashMap<String, ScrapeResponse> cache = new ConcurrentHashMap<String, ScrapeResponse>();
	
	private ScrapeResponseFactory() {
		
	}
	
	public static String scrape(final String requestEncoding, final String[] parameterValues) throws ScrapeException {
		if (parameterValues == null) {
			throw new ScrapeException(Errors.MISSING_INFO_HASH.toString());
		} else {
			String encoding;
			
			if (requestEncoding == null) {
				encoding = "UTF-8";
			} else {
				encoding = requestEncoding;
			}
			
			StringBuilder builder = new StringBuilder();
			
			String[] infohashes = parameterValues.clone();
			final int length = infohashes.length;
			
			try {
				for (int i = 0; i  < length; i++) {
					infohashes[i] = Data.byteArrayToHexString(infohashes[i].getBytes(encoding));
				}
			} catch (UnsupportedEncodingException e) {
				throw new ScrapeException(Errors.UNPARSEABLE_INFO_HASH.toString(), e);
			}
			
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

	private static ScrapeResponse getScrapeResponse(final String infohash) {
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
