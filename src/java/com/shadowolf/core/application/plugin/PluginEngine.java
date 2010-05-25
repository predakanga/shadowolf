package com.shadowolf.core.application.plugin;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import javolution.util.FastMap;
import javolution.util.FastSet;

import org.apache.log4j.Logger;

import com.shadowolf.core.application.announce.Announce;
import com.shadowolf.core.application.announce.AnnounceException;
import com.shadowolf.core.config.PluginConfig;
import com.shadowolf.util.Exceptions;

public class PluginEngine {
	private final static Logger LOGGER = Logger.getLogger(PluginEngine.class);
	private final static boolean DEBUG = true;
	
	private Map<Class<? extends AbstractPlugin>, AbstractPlugin> pluginRegistry 
			= new FastMap<Class<? extends AbstractPlugin>, AbstractPlugin>();
	
	private final Set<WeakReference<AbstractScheduledPlugin>> scheduledPlugins 
			= new FastSet<WeakReference<AbstractScheduledPlugin>>();
	private final Set<WeakReference<AnnounceAction>> announceActions 
			= new FastSet<WeakReference<AnnounceAction>>();
	private final Set<WeakReference<AnnounceFilter>> announceFilters 
			= new FastSet<WeakReference<AnnounceFilter>>();
	private final Set<WeakReference<AnnounceMutator>> announceMutators 
			= new FastSet<WeakReference<AnnounceMutator>>();
	private final ScheduledThreadPoolExecutor executor;
	
	public PluginEngine(Iterable<PluginConfig> config, ServletContext context) {
		if(DEBUG) {
			LOGGER.debug("Creating plugin engine.");
		}
		
		for(PluginConfig pConfig : config) {
			AbstractPlugin p = ConfigConsumer.consume(pConfig, context);
			
			this.pluginRegistry.put(p.getClass(), p);
			
			if(p instanceof AbstractScheduledPlugin) {
				this.scheduledPlugins.add(new WeakReference<AbstractScheduledPlugin>((AbstractScheduledPlugin)p));
			}
			
			if(p instanceof AnnounceFilter) {
				this.announceFilters.add(new WeakReference<AnnounceFilter>((AnnounceFilter) p));
			}
			
			if(p instanceof AnnounceAction) {
				this.announceActions.add(new WeakReference<AnnounceAction>((AnnounceAction) p));
			}
			
			if(p instanceof AnnounceMutator) {
				this.announceMutators.add(new WeakReference<AnnounceMutator>((AnnounceMutator) p));
			}
			
			p.init();
		}
		
		this.executor = new ScheduledThreadPoolExecutor(this.scheduledPlugins.size());
		
		if(DEBUG) {
			LOGGER.debug("Finished creating plugin engine.");
		}
		
	}
	
	public void initializeScheduledPlugins() {
		Iterator<WeakReference<AbstractScheduledPlugin>> iter = this.scheduledPlugins.iterator();
		while(iter.hasNext()) {
			AbstractScheduledPlugin reference = iter.next().get();
			if(reference == null) {
				iter.remove();
			} else {
				executor.scheduleAtFixedRate(reference, reference.getInitialDelay(), 
						reference.getPeriod(), reference.getUnit());
			}
		}
	}
	
	public void destroy() {
		if(!this.executor.isShutdown()) {
			this.executor.shutdown();
		}
		
		try {
			this.executor.awaitTermination(1800, TimeUnit.SECONDS);
			
			Iterator<WeakReference<AbstractScheduledPlugin>> iter = this.scheduledPlugins.iterator();
			while(iter.hasNext()) {
				AbstractScheduledPlugin reference = iter.next().get();
				if(reference == null) {
					iter.remove();
				} else {
					reference.destroy();
				}
			}
		} catch (InterruptedException e) {
			LOGGER.error(Exceptions.logInfo(e));
			throw new RuntimeException("Problem shutting down scheduled executor.  See log.");
		}
	}
	
	public void doAnnounce(final Announce announce) {
		Iterator<WeakReference<AnnounceAction>> iter = this.announceActions.iterator();
		while(iter.hasNext()) {
			AnnounceAction reference = iter.next().get();
			if(reference == null) {
				iter.remove();
			} else {
				reference.doAnnounce(announce);
			}
		}
	}
	
	public void filterAnnounce(final Announce announce) throws AnnounceException {
		Iterator<WeakReference<AnnounceFilter>> iter = this.announceFilters.iterator();
		while(iter.hasNext()) {
			AnnounceFilter reference = iter.next().get();
			if(reference == null) {
				iter.remove();
			} else {
				reference.filterAnnounce(announce);
			}
		}
	}
	
	public void mutateAnnounce(final Announce announce) {
		Iterator<WeakReference<AnnounceMutator>> iter = this.announceMutators.iterator();
		while(iter.hasNext()) {
			AnnounceMutator reference = iter.next().get();
			if(reference == null) {
				iter.remove();
			} else {
				reference.mutateAnnounce(announce);
			}
		}
	}
}
