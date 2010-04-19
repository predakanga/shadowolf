package com.shadowolf.api.users;

import org.apache.log4j.Logger;

import com.shadowolf.api.AbstractService;
import com.shadowolf.api.ResponseCodes;
import com.shadowolf.config.Config;
import com.shadowolf.plugins.scheduled.PasskeyEnforcer;

public class UserService extends AbstractService {
	private static final long serialVersionUID = 5613619344362552179L;
	private static final boolean DEBUG = true;
	private static final Logger LOGGER = Logger.getLogger(UserService.class);

	@Override
	public boolean putVerify(final String passKey) {
		if (DEBUG) {
			LOGGER.debug("Verifying PUT request for: " + passKey);
		}
		
		return passKey.matches("[A-Za-z0-9]+");
	}

	@Override
	public int putMethod(final String passKey) {
		if (DEBUG) {
			LOGGER.debug("PUT request for: " + passKey);
		}
		
		final PasskeyEnforcer passKeyEnforcer = Config.getPluginEngine()
				.getPlugin(PasskeyEnforcer.class);
		if (passKeyEnforcer == null) {
			return ResponseCodes.SUCCESS; //NOPMD
		} else {
			passKeyEnforcer.addPassKey(passKey);
			return ResponseCodes.SUCCESS;//NOPMD
		}
	}

	@Override
	public boolean deleteVerify(final String passKey) {
		if (DEBUG) {
			LOGGER.debug("Verifying DELETE request for: " + passKey);
		}
		
		return passKey.matches("[A-Za-z0-9]+");
	}

	@Override
	public int deleteMethod(final String passKey) {
		if (DEBUG) {
			LOGGER.debug("DELETE request for: " + passKey);
		}
		
		final PasskeyEnforcer passKeyEnforcer = Config.getPluginEngine()
				.getPlugin(PasskeyEnforcer.class);
		if (passKeyEnforcer == null) {
			return ResponseCodes.SUCCESS;//NOPMD
		} else {			
			if (passKeyEnforcer.removePassKey(passKey)) {
				return ResponseCodes.SUCCESS;//NOPMD
			} else {
				return ResponseCodes.NOTHING_TO_DELETE;//NOPMD
			}

		}
	}
}
