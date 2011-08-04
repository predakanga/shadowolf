package com.shadowolf.protocol;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import com.google.common.collect.MapMaker;
import com.shadowolf.ShadowolfComponent;
import com.shadowolf.ShadowolfContext;
import com.shadowolf.util.ReservoirSampler;

/**
 * This is the list of peers for a given torrent.
 * 
 * <p><strong>There are no guarantees that all peers in this list are valid!</strong>
 * This is made on a "best effort" basis. Removal from the list is dependent on stale
 * peers being garbaged collected, which may not happen immediately. Peers are eligible
 * for deletion after <code>protocol.Peerlist.peerExpiry</code> seconds, unless <code>put(InetAddress)</code>
 * is called on the corresponding InetAddress prior.</p>
 * 
 * 
 * <strong>THIS IS INCOMPLETE</strong>
 * @author Jon
 *
 */
@ThreadSafe
public class Peerlist implements ShadowolfComponent {
	private final ConcurrentMap<InetAddress,Peer> map;
	
	private ShadowolfContext context;
	
	public Peerlist(ShadowolfContext s) {
		setContext(s);
		
		MapMaker m = new MapMaker();
		m.weakValues();
		m.expireAfterWrite(context.getConfiguration().getLong("protocol.Peerlist.peerExpiry", 2400L), TimeUnit.SECONDS);
		map = m.makeMap();
	}
	
	public Peer put(InetAddress i) {
		Peer p = get(i);
		if(p == null) {
			p = createPeer(i);
		}
		
		map.put(i,p);
		
		return p;
	}
	
	public Peer get(InetAddress i) {
		return map.get(i);
	}
	
	private Peer createPeer(InetAddress i) {
		return new Peer(i);
	}
	
	public List<Peer> getPeers(int i) {
		return ReservoirSampler.listSample(map.values(), i);
		
	}
	
	@Override
	public void setContext(ShadowolfContext c) {
		context = c;
	}

	@Override
	public ShadowolfContext getContext() {
		return context;
	}
}
