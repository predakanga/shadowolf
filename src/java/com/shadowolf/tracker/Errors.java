/**
 * 
 */
package com.shadowolf.tracker;

import com.shadowolf.config.Config;

public enum Errors {
	TORRENT_NOT_REGISTERED {
		@Override
		public String toString() {
			return TrackerResponse.bencoded("Torrent is not registered with this tracker");
		}
	},

	INVALID_PASSKEY {
		@Override
		public String toString() {
			return TrackerResponse.bencoded("Unrecognized passkey");
		}
	},
	
	UNPARSEABLE_INFO_HASH {
		@Override
		public String toString() {
			return TrackerResponse.bencoded("Failed to parse the info-hash your client passed.");
		}
	},

	BANNED_CLIENT {
		@Override
		public String toString() {
			return TrackerResponse.bencoded("Your client is banned.  Please upgrade to a client on the whitelist.");
		}
	},


	MISSING_PORT {
		@Override
		public String toString() {
			return TrackerResponse.bencoded("Your client did not send required parameter: port");
		}
	},

	MISSING_INFO_HASH {
		@Override
		public String toString() {
			return TrackerResponse.bencoded("Your client did not send required parameter: info_hash");
		}
	},

	MISSING_PASSKEY {
		@Override
		public String toString() {
			return TrackerResponse.bencoded("Your client did not send required parameter: passkey");
		}
	},

	MISSING_PEER_ID {
		@Override
		public String toString() {
			return TrackerResponse.bencoded("Your client did not send required parameter: peer_id");
		}
	},

	TOO_MANY_LOCATIONS {
		@Override
		public String toString() {
			return TrackerResponse.bencoded("You're already seeding from " + Config.getParameter("user.max_locations") + " locations");
		}
	},

	UNEXPECTED_4_PEER_LENGTH {
		@Override
		public String toString() {
			return TrackerResponse.bencoded("IPv4 address: unexpected length");
		}
	},

	UNEXPECTED_6_PEER_LENGTH {
		@Override
		public String toString() {
			return TrackerResponse.bencoded("IPv6 address: unexpected length");
		}
	}
}