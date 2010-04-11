package com.shadowolf.announce;

public enum Event {
	STARTED {
		@Override
		public String toString() {
			return "started";
		}
	},
	STOPPED {
		@Override
		public String toString() {
			return "stopped";
		}
	},
	ANNOUNCE {
		@Override
		public String toString() {
			return "announce";
		}
	},
	COMPLETED {
		@Override
		public String toString() {
			return "completed";
		}
	}
}
