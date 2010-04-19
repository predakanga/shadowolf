package com.shadowolf.api.torrents;

import org.apache.log4j.Logger;

import com.shadowolf.api.AbstractService;
import com.shadowolf.api.ResponseCodes;
import com.shadowolf.config.Config;
import com.shadowolf.plugins.scheduled.InfoHashEnforcer;

public class TorrentService extends AbstractService {
	private static final long serialVersionUID = 9122130682907965177L;
	private static final boolean DEBUG = true;
	private static final Logger LOGGER = Logger.getLogger(TorrentService.class);
	
	@Override
	protected int deleteMethod(String inputLine) {
		InfoHashEnforcer plugin = Config.getPluginEngine().getPlugin(InfoHashEnforcer.class);
		
		if(plugin.removeHash(inputLine)) {
			return ResponseCodes.SUCCESS;
		} else  {
			return ResponseCodes.NOTHING_TO_DELETE;
		}
	}

	@Override
	protected boolean deleteVerify(String inputLine) {
		return inputLine.length() == 40 && inputLine.matches("[a-zA-Z0-9]+");
	}

	@Override
	protected int putMethod(String inputLine) {
		if(DEBUG) {
			LOGGER.debug("PUT request for: " + inputLine);
		}
		InfoHashEnforcer plugin = Config.getPluginEngine().getPlugin(InfoHashEnforcer.class);
		plugin.addHash(inputLine);
		return ResponseCodes.SUCCESS;
	}

	@Override
	protected boolean putVerify(String inputLine) {
		if(DEBUG) {
			LOGGER.debug("Verifying PUT request for: " + inputLine);
		}
		return inputLine.length() == 40 && inputLine.matches("[a-zA-Z0-9]+");
	}
}