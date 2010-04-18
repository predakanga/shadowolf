package com.shadowolf.scrape;

import static org.junit.Assert.*;

import org.junit.Test;

import com.shadowolf.tracker.ScrapeException;

public class ScrapeResponseFactoryTest {
	private String info_hash = "4Vx¼Þñ#Eg«Íï4Vx";

	@Test
	public void testScrape() {
		final String expected = "d5:filesd20:4Vx¼Þñ#Eg«Íï4Vxd8:completei0e10:downloadedi0e10:incompletei0eeee";
		try {
			final String received = ScrapeResponseFactory.scrape("UTF-8", new String[]{info_hash});
			assertEquals(expected, received);
		} catch (ScrapeException e) {
			fail("Unexpected ScrapeException:" +e.toString());
		}
	}
	
	@Test
	public void testScrapeEncodingNull() {
		final String expected = "d5:filesd20:4Vx¼Þñ#Eg«Íï4Vxd8:completei0e10:downloadedi0e10:incompletei0eeee";
		try {
			final String received = ScrapeResponseFactory.scrape(null, new String[]{info_hash});
			assertEquals(expected, received);
		} catch (ScrapeException e) {
			fail("Unexpected ScrapeException:" +e.toString());
		}
	}
	
	@Test
	public void testScrapeInfoHashNull() {
		try {
			ScrapeResponseFactory.scrape(null, null);
			fail("Expected ScrapeException to be thrown, not thrown");
		} catch (ScrapeException e) {
		}
	}
	
	@Test
	public void testSubsequestGetsResultInSameFetchTime() {
		try {
			final long fetch1 = ScrapeResponseFactory.getScrapeResponse(info_hash, null).getFetchTime();
			final long fetch2 = ScrapeResponseFactory.getScrapeResponse(info_hash, null).getFetchTime();
			
			assertEquals(fetch1, fetch2);
		} catch (ScrapeException e) {
			fail("Unexpected ScrapeException: "+e.toString());
		}
	}

	@Test
	public void testRemove() {
		try {
			final long fetch1 = ScrapeResponseFactory.getScrapeResponse(info_hash, null).getFetchTime();
			
			ScrapeResponseFactory.remove(info_hash);
			
			final long fetch2 = ScrapeResponseFactory.getScrapeResponse(info_hash, null).getFetchTime();
			
			assertFalse(fetch1 == fetch2);
		} catch (ScrapeException e) {
			fail("Unexpected ScrapeException: "+e.toString());
		}
	}
}
