package com.shadowolf;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.Immutable;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.common.collect.MapMaker;
import com.shadowolf.plugin.Plugin;
import com.shadowolf.plugin.PluginEngine;
import com.shadowolf.plugin.PluginLoader;
import com.shadowolf.protocol.Infohash;
import com.shadowolf.protocol.Peerlist;
import com.shadowolf.server.AccessList;
import com.shadowolf.server.JettyServer;
import com.shadowolf.util.Exceptions;

/**
 * Class that represents a runtime context of the application.  
 * 
 * <br/><br/>
 * 
 * Instances of this class, for all intents and purposes, encapsulate
 * all of the logic and state necessary to consider an object a running
 * application.  Creating a new instance should be synonymous with
 * starting a new tracker on a new socket.  The provided factory
 * method creates and initializes an object with the default
 * configuration location and names. Custom behavior can be achieved
 * by subclassing.
 * 
 * <br/><br/>
 * 
 * By encapsulating all of the runtime logic like we have here, we
 * will be able to provide some nice services to our users, like
 * being able to restart the tracker without restarting the application
 * for all of the benefits that that offers.  In the future, we 
 * may support "multi-core" trackers that function as a cluster 
 * in a single application. 
 * 
 * <strong>This class, itself, is threadsafe because it's immutable.
 * 	This class provides no guarantees about the thread safety of methods
 *  invoked on this class's members</strong>
 *   
 * <strong>The state of this class is currently transient
 * 	and may change at any moment!</strong>
 */

@Immutable
public class ShadowolfContext {
	private ServerContext serverContext = new ServerContext();
	private PluginContext pluginContext = new PluginContext();
	private BittorrentContext bittorrentContext = new BittorrentContext();
	
	private Configuration configuration;
	
	protected ShadowolfContext() {
		
	}

	public Configuration getConfiguration() {
		return configuration;
	}
	protected void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public ServerContext getServerContext() {
		return serverContext;
	}

	public PluginContext getPluginContext() {
		return pluginContext;
	}

	public BittorrentContext getBittorrentContext() {
		return bittorrentContext;
	}

	public void start() throws Exception {
		pluginContext.getPluginEngine().startScheduler();
		serverContext.getJettyServer().startServer(serverContext.getServerAccessList());
		serverContext.getJettyServer().join();
	}
	
	/**
	 * This static factory method is huge, and it is ugly.  It creates
	 * an entire context for an application!
	 */
	public static ShadowolfContext createNewContext(String configFile) {
		ShadowolfContext context = new ShadowolfContext();
		
		try {
			context.configuration = new PropertiesConfiguration(configFile);
		} catch (ConfigurationException e) {
			Exceptions.log(e);
			throw new RuntimeException(e);
		}
		
		//initialize ServerContext
		try {
			ServerContext serverContext = new ServerContext();
			InetAddress address = InetAddress.getByName(context.configuration.getString("server.listen.address", "127.0.0.1"));
			int port = context.configuration.getInt("server.listen.port", 80);
			serverContext.setJettyServer(new JettyServer(address, port));
			
			int corePoolSize = context.configuration.getInt("server.workers.min", 4);
			int maximumPoolSize = context.configuration.getInt("server.workers.max", 16);
			int keepAliveTime = context.configuration.getInt("server.workers.idle", 300);
			int queueLength = context.configuration.getInt("server.workers.queueSize", 25_000);
			BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(queueLength);
			TimeUnit unit = TimeUnit.SECONDS;

			ExecutorService es = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
			serverContext.setHttpWorkerPool(es);
			serverContext.getJettyServer().setExecutor(es);
			
			String conf = "conf/serverAccess.xml";
			JAXBContext jaxb = JAXBContext.newInstance(AccessList.class);
			Unmarshaller um = jaxb.createUnmarshaller();
			serverContext.setServerAccessList((AccessList) um.unmarshal(new File(conf)));
			
			context.serverContext = serverContext;
		} catch (UnknownHostException | ServletException | JAXBException e) {
			Exceptions.log(e);
			throw new RuntimeException(e);
		}
		
		//initialize PluginContext 
		{
			PluginContext pluginContext = new PluginContext();
			
			int corePoolSize = context.configuration.getInt("plugins.async.workers.min", 4);
			int maximumPoolSize = context.configuration.getInt("plugins.async.workers.max", 16);
			int keepAliveTime = context.configuration.getInt("plugins.async.workers.idle", 300);
			int queueLength = context.configuration.getInt("plugins.async.workers.queueSize", 25_000);
			BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(queueLength);
			TimeUnit unit = TimeUnit.SECONDS;
			
			ExecutorService es = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);

			PluginLoader loader = new PluginLoader();
			loader.setContext(context);
			loader.loadAllPluginsInDir();
			
			PluginEngine engine = new PluginEngine(es);
			for(Plugin p : loader.getPlugins().values()) {
				engine.addPlugin(p);
			}
			
			pluginContext.setAsyncPluginWorkerPool(es);
			pluginContext.setPluginEngine(engine);
			pluginContext.setPluginLoader(loader);
			context.pluginContext = pluginContext;
		}
		
		//initialize BittorrentContext
		{
			BittorrentContext bittorrentContext = new BittorrentContext();
		
			int peerExpiry = context.getConfiguration().getInt("protocol.Peerlist.peerExpiry", 2400);
			int httpWorkers = context.getConfiguration().getInt("server.workers.max", 16);
			// 1/8th of all workers accessing the same peerlist seems unlikely
			// and a concurrency level of six seems sane... these values might need to be tuned later
			int concurrencyLevel = (httpWorkers/8) > 6 ? 6 : httpWorkers/8; 
			
			//the largest majority of torrents that will ever be tracked will have
			//less than 2 peers, so reducing the default size means that we'll have
			//slightly less memory overhead - in exchange for a bunch of resize ops
			bittorrentContext.setPeerlistMapMaker(
				new MapMaker().
				expireAfterWrite(peerExpiry, TimeUnit.SECONDS).
				concurrencyLevel(concurrencyLevel).
				initialCapacity(2)
			);
			
			ConcurrentMap<Infohash, Peerlist> plists = 
					new MapMaker().
					expireAfterAccess(peerExpiry, TimeUnit.SECONDS).
					concurrencyLevel(concurrencyLevel).
					makeMap();
			bittorrentContext.setPeerlists(plists);
			
			context.bittorrentContext = bittorrentContext;
		}
		
		return context;
	}
	
	public static class ServerContext {
		private JettyServer jettyServer;
		private ExecutorService httpWorkerPool;
		private AccessList serverAccessList;
		
		public ExecutorService getHttpWorkerPool() {
			return httpWorkerPool;
		}
		protected void setHttpWorkerPool(ExecutorService httpWorkerPool) {
			this.httpWorkerPool = httpWorkerPool;
		}
		
		public JettyServer getJettyServer() {
			return jettyServer;
		}
		protected void setJettyServer(JettyServer jettyServer) {
			this.jettyServer = jettyServer;
		}
		
		public AccessList getServerAccessList() {
			return serverAccessList;
		}
		public void setServerAccessList(AccessList serverAccessList) {
			this.serverAccessList = serverAccessList;
		}
	}
	
	public static class PluginContext {
		private PluginLoader pluginLoader;
		private PluginEngine pluginEngine;
		private ExecutorService asyncPluginWorkerPool;
		
		public PluginLoader getPluginLoader() {
			return pluginLoader;
		}
		protected void setPluginLoader(PluginLoader pluginLoader) {
			this.pluginLoader = pluginLoader;
		}

		public PluginEngine getPluginEngine() {
			return pluginEngine;
		}
		protected void setPluginEngine(PluginEngine pluginEngine) {
			this.pluginEngine = pluginEngine;
		}
		
		public ExecutorService getAsyncPluginWorkerPool() {
			return asyncPluginWorkerPool;
		}
		protected void setAsyncPluginWorkerPool(ExecutorService asyncPluginWorkerPool) {
			this.asyncPluginWorkerPool = asyncPluginWorkerPool;
		}
	}
	
	public static class BittorrentContext {
		private MapMaker peerlistMapMaker;
		private ConcurrentMap<Infohash, Peerlist> peerlists;
		
		public MapMaker getPeerlistMapMaker() {
			return peerlistMapMaker;
		}
		public void setPeerlistMapMaker(MapMaker peerlistMapMaker) {
			this.peerlistMapMaker = peerlistMapMaker;
		}
		
		public ConcurrentMap<Infohash, Peerlist> getPeerlists() {
			return peerlists;
		}
		public void setPeerlists(ConcurrentMap<Infohash, Peerlist> peerlists) {
			this.peerlists = peerlists;
		}
	}
}
