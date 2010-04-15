package com.shadowolf.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;


public class Config {
	private static final boolean DEBUG = true;
	private static final Logger LOGGER = Logger.getLogger(Config.class);

	public static final String DEFAULT_CONF_PATH = "/WEB-INF/config.xml";

	private static List<PluginConfig> plugins = new ArrayList<PluginConfig>();
	private static Map<String, String> parameters = new HashMap<String, String>();
	
	private static boolean init = false;
	private static Object boolLock = new Object();

	public static String getParameter(final String param) {
		return parameters.get(param).trim();
	}
	
	public static boolean isInitialized() {
		synchronized(boolLock) {
			return init;
		}
	}

	public static void init(final ServletContext context) {
		if (init == false) {
			synchronized(boolLock) {
				String path = context.getRealPath(DEFAULT_CONF_PATH);

				//convert XML to an object
				if(System.getProperty("com.shadowolf.config.path") != null) {
					path = System.getProperty("com.shadowolf.config.path");
				}

				Parser configParser;
				try {
					configParser = new Parser(path);
					//parse config object
					final Element configRoot = configParser.getRootElement();
					Config.parseConfigElement(configRoot);
					init = true;
				} catch (ParserConfigurationException e) {
					LOGGER.error(e.getClass().toString() + "  parsing configuration: " + e.getMessage());
				} catch (SAXException e) {
					LOGGER.error(e.getClass().toString() + "  parsing configuration: " + e.getMessage());
				} catch (IOException e) {
					LOGGER.error(e.getClass().toString() + "  parsing configuration: " + e.getMessage());
				}
			}
		}
	}
	
	public static void parseConfigElement(final Element rootElement) {
		if(!rootElement.hasChildren()) {
			return;
		}

		for(final Element e : rootElement.getChildren()) {
			if(e.getName().equals("plugin")) {
				if(DEBUG) {
					LOGGER.debug("Adding plugin: " + e.getAttribute("class"));
				}
				final PluginConfig pConf = new PluginConfig(e.getAttribute("class"));

				if(e.hasChildren()) {
					for(final Element option : e.getChildren()) {
						if((option.getText() == null) || option.getText().trim().equals("")) {
							continue;
						}

						if(DEBUG) {
							LOGGER.debug(option.getAttribute("name") + option.getText().trim());
						}
						if(option.getAttribute("name").equals("unit")) {
							pConf.setTimeUnit(option.getText().trim());
							if(DEBUG) {
								LOGGER.debug("Setting unit to: " + option.getText().trim());
							}
						} else if(option.getAttribute("name").equals("period")) {
							if(DEBUG) {
								LOGGER.debug("Setting period to: " + option.getText().trim());
							}
							pConf.setPeriod(option.getText().trim());
						} else if(option.getAttribute("name").equals("delay")) {
							if(DEBUG) {
								LOGGER.debug("Setting delay to: " + option.getText().trim());
							}
							pConf.setDelay(option.getText().trim());
						} else {
							if(DEBUG) {
								LOGGER.debug("Adding option: " + option.getAttribute("name") + " with text: " + option.getText().trim());
							}
							pConf.setParameter(option.getAttribute("name"), option.getText().trim());
						}
					}
				}

				plugins.add(pConf);
			} else if (e.getName().equals("parameter")) {
				parameters.put(e.getAttribute("name"), e.getText());
				if(DEBUG) {
					LOGGER.debug("Adding parameter: " + e.getAttribute("name") + " with text: " + e.getText().trim());
				}
			}
		}
	}


	public static List<PluginConfig> getPlugins() {
		return plugins;
	}


	public static Map<String, String> getParameters() {
		return parameters;
	}

}
