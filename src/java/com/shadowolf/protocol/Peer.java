package com.shadowolf.protocol;

import java.net.InetAddress;

import javax.annotation.concurrent.ThreadSafe;

/**
 * TBD
 *
 */
@ThreadSafe
public class Peer {
	private final InetAddress inet;
	
	Peer(InetAddress i) {
		inet = i;
	}
}
