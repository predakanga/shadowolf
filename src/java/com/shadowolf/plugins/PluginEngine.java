package com.shadowolf.plugins;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javolution.util.FastMap;
import javolution.util.FastSet;

import org.apache.log4j.Logger;

import com.shadowolf.announce.Announce;
import com.shadowolf.tracker.AnnounceException;

public class PluginEngine {
	private static final boolean DEBUG = true;
	private static final Logger LOGGER = Logger.getLogger(PluginEngine.class);
	private final ScheduledThreadPoolExecutor schedExecutor; //NOPMD ... not a bean
	private final Set<WeakReference<AnnounceFilter>> announceFilters = new FastSet<WeakReference<AnnounceFilter>>();
	private final Set<WeakReference<AnnounceAction>> announceActions = new FastSet<WeakReference<AnnounceAction>>(); //NOPMD ... not a bean

	private final Set<Plugin> plugins = new FastSet<Plugin>();
	private final Set<ScheduledPlugin> scheduled = new FastSet<ScheduledPlugin>();

	private final Map<Class<? extends Plugin>, WeakReference<? extends Plugin>> registry =
		new FastMap<Class<? extends Plugin>, WeakReference<? extends Plugin>>();

	@SuppressWarnings("unchecked")
	public PluginEngine(final Plugin... plugins) {
		for(final Plugin p : plugins) {
			final Class<Plugin> clazz = (Class<Plugin>)p.getClass();
			
			if(p instanceof AnnounceFilter) {
				if(DEBUG) {
					LOGGER.debug("Added AnnounceFilter: " + p.getClass());
				}
				this.announceFilters.add(new WeakReference<AnnounceFilter>((AnnounceFilter)p));
			}
			
			if(p instanceof AnnounceAction) {
				if(DEBUG) {
					LOGGER.debug("Added AnnounceAction: " + p.getClass());
				}
				this.announceActions.add(new WeakReference<AnnounceAction>((AnnounceAction)p));
			}

			if(p instanceof ScheduledPlugin) {
				this.scheduled.add((ScheduledPlugin)p);
			} else {
				this.plugins.add(p);
			}

			this.registry.put(clazz, new WeakReference<Plugin>(p));
		}

		this.schedExecutor = new ScheduledThreadPoolExecutor(this.scheduled.size());
	}

	@SuppressWarnings("unchecked")
	public <T extends Plugin>T getPlugin(final Class<T> clazz) {
		final T p = (T)(this.registry.get(clazz).get());
		if(p == null) {
			this.registry.remove(clazz);
		}

		return p;
	}

	public void execute() {
		//deal with scheduler plugins
		for(ScheduledPlugin plugin : scheduled) {
			if(DEBUG) {
				LOGGER.debug("Class: " + plugin.getClass());
				LOGGER.debug("Delay: " + plugin.getInitialDelay());
				LOGGER.debug("Period: " + plugin.getPeriod());
				LOGGER.debug("Unit: " + plugin.getUnit());
			}
			this.schedExecutor.scheduleAtFixedRate(
					plugin, plugin.getInitialDelay(), plugin.getPeriod(), plugin.getUnit());
		}
	}

	public void doAnnounce(final Announce announce) throws AnnounceException {
		for(WeakReference<AnnounceFilter> filterRef : this.announceFilters) {
			final AnnounceFilter filter = filterRef.get();
			
			if(filter != null) {
				filter.filterAnnounce(announce);
			}
		}
		
		for(WeakReference<AnnounceAction> announceRef : this.announceActions) {
			final AnnounceAction action = announceRef.get();
			
			if(action != null) {
				action.doAnnounce(announce);
			}
		}
	}

	public synchronized void destroy() {
		if(DEBUG) {
			LOGGER.debug("Destroying... Current status: " + this.schedExecutor.isShutdown());
		}
		
		if(!this.schedExecutor.isShutdown()) {
			this.schedExecutor.shutdownNow();
		}
		
		for(ScheduledPlugin p : this.scheduled) {
			p.run();
		}
		
		if(DEBUG) {
			LOGGER.debug("Destroying... Current status: " + this.schedExecutor.isShutdown());
		}
		
	}
}