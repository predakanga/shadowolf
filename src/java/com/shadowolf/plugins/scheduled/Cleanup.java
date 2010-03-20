package com.shadowolf.plugins.scheduled;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.shadowolf.plugins.ScheduledPlugin;
import com.shadowolf.user.PeerListFactory;
import com.shadowolf.user.UserFactory;

public class Cleanup extends ScheduledPlugin  {
	private static final Logger LOGGER = Logger.getLogger(Cleanup.class);
	
	public Cleanup(Attributes attributes) {
		super(attributes);
		LOGGER.debug("Instantiating sweeping!");
	}

	@Override
	public void run() {
		LOGGER.debug("Sweeping...");
		PeerListFactory.cleanUp();
		UserFactory.cleanUp();
		LOGGER.debug("Swept");
	}

}
