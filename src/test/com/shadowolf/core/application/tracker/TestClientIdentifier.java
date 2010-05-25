package com.shadowolf.core.application.tracker;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javolution.util.FastMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.shadowolf.core.application.tracker.ClientIdentifier;

public class TestClientIdentifier {
	private ClientIdentifier id1;
	private ClientIdentifier id2;
	private ClientIdentifier id3;

	@Test
	public void equality() {
		assertTrue(id1.equals(id1));
		assertTrue(id1.equals(id2));
		assertTrue(id2.equals(id1));
		assertTrue(!id1.equals(id3));
		assertTrue(!id3.equals(id1));
	}
	
	@Test 
	public void hashCodeEquality() {
		assertTrue(id1.hashCode() == id2.hashCode());
		assertTrue(id1.hashCode() != id3.hashCode());
	}

	@Test
	public void fastMapIndex() {
		final Map<ClientIdentifier, String> testMap = new FastMap<ClientIdentifier, String>();

		testMap.put(this.id1, "Exists!");

		assertNotNull(testMap.get(this.id1));
		assertNotNull(testMap.get(this.id2));
		assertNull(testMap.get(id3));
	}

	@Before
	public void setUp() throws Exception {
		final byte[] port1 = new byte[] { 0x00, 0x01 };
		final byte[] port2 = new byte[] { 0x00, 0x01 };
		final byte[] ipAddress1 = new byte[] { 0x00, 0x01, 0x02, 0x03 };
		final byte[] ipAddress2 = new byte[] { 0x00, 0x01, 0x02, 0x03 };
		String passkey = "testPasskey";

		this.id1 = new ClientIdentifier(ipAddress1, port1, passkey);
		this.id2 = new ClientIdentifier(ipAddress2, port2, passkey);
		id3 = new ClientIdentifier(ipAddress1, port1, "notTheSame");
	}

	@After
	public void tearDown() throws Exception {
	}

}
