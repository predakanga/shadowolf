package com.shadowolf.tracker;

public class TrackerRequest {
	public enum Event {
		STARTED {
			public String toString() {
				return "started";
			}
		},
		STOPPED { 
			public String toString() {
				return "stopped";
			}
		},
		ANNOUNCE {
			public String toString() {
				return "announce";
			}
		},
		COMPLETED {
			public String toString() {
				return "completed";
			}
		}
	}
}
