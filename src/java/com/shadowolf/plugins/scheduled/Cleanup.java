package com.shadowolf.plugins.scheduled;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.shadowolf.plugins.ScheduledPlugin;
import com.shadowolf.user.PeerListFactory;
import com.shadowolf.user.UserFactory;

public class Cleanup extends ScheduledPlugin  {
	private static final Logger LOGGER = Logger.getLogger(Cleanup.class);
	private static final boolean DEBUG = false;
	
	public Cleanup(final Attributes attributes) {
		super(attributes);
		if(DEBUG) { 
			LOGGER.debug("Instantiating sweeping!");
		}
	}

	@Override
	public void run() {
		if(DEBUG) {
			LOGGER.debug("Sweeping...");
		}
		
		PeerListFactory.cleanUp();
		UserFactory.cleanUp();
		
		if(DEBUG) {
			LOGGER.debug("Swept");
		}
	}

}
