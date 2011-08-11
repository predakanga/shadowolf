package com.shadowolf.protocol;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.concurrent.ThreadSafe;

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
	private ConcurrentMap<InetSocketAddress,Peer> map;
	
	private ShadowolfContext context;
	
	public Peerlist(ShadowolfContext s) {
		setContext(s);
		map = s.getBittorrentContext().getPeerlistMapMaker().makeMap();
	}
	
	public boolean touchPeer(InetSocketAddress address) {
		Peer peer = map.get(address);
		
		if(peer == null) {
			return false;
		}
		
		peer.touch();
		map.put(address, peer);
		return true;
	}
	
	public Peer put(InetSocketAddress address) {
		Peer p = get(address);
		
		if(p == null) {
			p = createPeer(address);
		}
		
		map.put(address, p);
		
		return p;
	}
	
	public Peer get(InetSocketAddress address) {
		return map.get(address);
	}
	
	private Peer createPeer(InetSocketAddress address) {
		return new Peer(address);
	}
	
	public Collection<Peer> getPeers(int numwant, boolean preferLeechers) {
		if(numwant > map.size()) {
			return ReservoirSampler.listSample(map.values(), numwant);
		} else {
			return map.values();
		}
		
		
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
