package com.shadowolf.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.shadowolf.tracker.TrackerRequest.Event;

public class PluginEngine {
	private static final Logger LOGGER = Logger.getLogger(PluginEngine.class);
	private final HashMap<Plugin.Type, ArrayList<Plugin>> plugins = new HashMap<Plugin.Type, ArrayList<Plugin>>();
	private final ScheduledThreadPoolExecutor schedExecutor;
	private final HashSet<Plugin> announcers = new HashSet<Plugin>();
	
	public PluginEngine(String pathToXML) throws PluginException {
		try {
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(pathToXML, new PluginParser());
		} catch (Exception e) {
			//we so lazy
			throw new PluginException(e);
		}
		
		this.schedExecutor = new ScheduledThreadPoolExecutor(this.plugins.get(Plugin.Type.periodicThread).size());
	}
	
	public void execute() {
		//deal with scheduler plugins
		Iterator<Plugin> i = this.plugins.get(Plugin.Type.periodicThread).iterator();
		
		while(i.hasNext()) {
			ScheduledPlugin p = (ScheduledPlugin)i.next();
			this.schedExecutor.scheduleAtFixedRate(p, p.getInitialDelay(), p.getPeriod(), p.getUnit());
		}
	}
	
	public void doAnnounce(Event e, long uploaded, long downloaded, String passkey) {
		Iterator<Plugin> i = announcers.iterator();
		
		while(i.hasNext()) {
			i.next().doAnnounce(e, uploaded, downloaded, passkey);
		}
	}
	
	public void destroy() {
		this.schedExecutor.shutdown();
	}
	
	private class PluginParser extends DefaultHandler {
		
		@SuppressWarnings("unchecked")
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.equalsIgnoreCase("plugin")) {
				try {
					Class<Plugin> pluginClass = (Class<Plugin>) Class.forName(attributes.getValue("class"));
					Plugin plugin = pluginClass.getConstructor(new Class[] { Attributes.class }).newInstance(attributes);
					if(plugins.get(plugin.getType()) == null) {
						plugins.put(plugin.getType(), new ArrayList<Plugin>());
					}
					
					if(plugin.needsAnnounce()) {
						announcers.add(plugin);
					}
					plugins.get(plugin.getType()).add(plugin);
				} catch (Exception e) {
					LOGGER.error("Error parsing plugin!");
					//System.exit(1);
				}
			}
		}
	}
}
