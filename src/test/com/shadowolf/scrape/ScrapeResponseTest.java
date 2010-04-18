package com.shadowolf.scrape;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ScrapeResponseTest {
	private String info_hash = "d63b142cd67367058baecb36269c25bbf9bf37e5";
	private ScrapeResponse response;
	
	@Before
	public void setUp() throws Exception {
		this.response = new ScrapeResponse(info_hash, info_hash);
	}

	@Test
	public void testScrapeResponseString() {
		assertEquals(info_hash, response.getInfoHash());
		assertFalse(response.isExpired());
	}

	@Test
	public void testGetResponseString() {
		final String testString = "d20:d63b142cd67367058baecb36269c25bbf9bf37e5d8:completei0e10:downloadedi0e10:incompletei0eee";
		assertEquals(testString, response.getResponseString());
	}

}
