package com.shadowolf.server;

import com.shadowolf.util.concurrent.NamedThreadFactory;

/**
 * Utitlity instance of {@link NamedThreadFactory} that creates
 * threads for the primary server.
 */
public class ServerThreadFactory extends NamedThreadFactory {
	public ServerThreadFactory() {
		super("shadowolf-server-threads");
	}
}
