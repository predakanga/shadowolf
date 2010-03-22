package com.shadowolf.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.shadowolf.announce.Announce;
import com.shadowolf.tracker.AnnounceException;

public class PluginEngine {
	private static final Logger LOGGER = Logger.getLogger(PluginEngine.class);
	private final Map<Plugin.Type, ArrayList<Plugin>> plugins = new HashMap<Plugin.Type, ArrayList<Plugin>>(); //NOPMD ... not a bean
	private final ScheduledThreadPoolExecutor schedExecutor; //NOPMD ... not a bean
	private final Set<AnnounceFilter> announcers = new HashSet<AnnounceFilter>(); //NOPMD ... not a bean
	
	public PluginEngine(final String pathToXML) throws PluginException {
		try {
			final SAXParser sparser = SAXParserFactory.newInstance().newSAXParser();
			sparser.parse(pathToXML, new PluginParser());
		} catch (final Exception e) {
			//we so lazy
			throw new PluginException(e);
		}
		
		this.schedExecutor = new ScheduledThreadPoolExecutor(this.plugins.get(Plugin.Type.periodicThread).size());
	}
	
	public void execute() {
		//deal with scheduler plugins
		final Iterator<Plugin> iter = this.plugins.get(Plugin.Type.periodicThread).iterator();
		
		while(iter.hasNext()) {
			final ScheduledPlugin plugin = (ScheduledPlugin)iter.next();
			this.schedExecutor.scheduleAtFixedRate(plugin, plugin.getInitialDelay(), plugin.getPeriod(), plugin.getUnit());
		}
	}
	
	public void doAnnounce(final Announce announce) throws AnnounceException {
		
		final Iterator<AnnounceFilter> iter = announcers.iterator();
		
		while(iter.hasNext()) {
			iter.next().doAnnounce(announce);
		}
	}
	
	public void destroy() {
		this.schedExecutor.shutdown();
	}
	
	private class PluginParser extends DefaultHandler {
		
		@SuppressWarnings("unchecked")
		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			if (qName.equalsIgnoreCase("plugin")) {
				try {
					final Class<Plugin> pluginClass = (Class<Plugin>) Class.forName(attributes.getValue("class"));
					
					final 	Plugin plugin = pluginClass.getConstructor(new Class[] { Attributes.class }).newInstance(attributes);
					
                   	//check if implements AnnounceFilter
                   	for(Class<?> c : pluginClass.getInterfaces()) {
						if("com.shadowolf.plugins.AnnounceFilter".equals(c.getCanonicalName())) {
							announcers.add((AnnounceFilter)plugin);
						}
					}
                   	
					if(plugins.get(plugin.getType()) == null) {
						plugins.put(plugin.getType(), new ArrayList<Plugin>());
					}
					
					plugins.get(plugin.getType()).add(plugin);
				} catch (Exception e) {
					LOGGER.error("Error parsing plugin!\t" + e.getMessage());
				}
			}
		}
	}
}
