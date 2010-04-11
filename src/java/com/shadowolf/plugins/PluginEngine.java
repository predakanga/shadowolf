package com.shadowolf.plugins;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.shadowolf.announce.Announce;
import com.shadowolf.tracker.AnnounceException;

public class PluginEngine {
	private static final boolean DEBUG = true;
	private static final Logger LOGGER = Logger.getLogger(PluginEngine.class);
	private final ScheduledThreadPoolExecutor schedExecutor; //NOPMD ... not a bean
	private final Set<WeakReference<AnnounceFilter>> announcers = new HashSet<WeakReference<AnnounceFilter>>(); //NOPMD ... not a bean
	
	private final Set<Plugin> plugins = new HashSet<Plugin>();
	private final Set<Plugin> scheduled = new HashSet<Plugin>();
	
	@SuppressWarnings("unchecked")
	public PluginEngine(Plugin... plugins) {
		for(Plugin p : plugins) {
			final Class<Plugin> clazz = (Class<Plugin>)p.getClass();
			
			for(Class<?> c : clazz.getInterfaces()) {
				if("com.shadowolf.plugins.AnnounceFilter".equals(c.getCanonicalName())) {
					if(DEBUG) {
						LOGGER.debug("Added AnnounceFilter: " + p.getClass());
					}
					announcers.add(new WeakReference<AnnounceFilter>((AnnounceFilter)p));
				}
			}
			
			if(p instanceof ScheduledPlugin) {
				this.scheduled.add(p);
			} else {
				this.plugins.add(p);
			}
			
		}
		
		this.schedExecutor = new ScheduledThreadPoolExecutor(announcers.size());
	}
	
	public void execute() {
		//deal with scheduler plugins
		final Iterator<Plugin> iter = this.scheduled.iterator();
		
		while(iter.hasNext()) {
			final ScheduledPlugin plugin = (ScheduledPlugin)iter.next();
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
		
		final Iterator<WeakReference<AnnounceFilter>> iter = announcers.iterator();
		
		while(iter.hasNext()) {
			AnnounceFilter announcer = iter.next().get();
			
			if(announcer != null) {
				announcer.doAnnounce(announce);
			} 
		}
	}
	
	public void destroy() {
		this.schedExecutor.shutdown();
	}
}