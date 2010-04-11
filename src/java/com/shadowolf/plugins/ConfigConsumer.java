package com.shadowolf.plugins;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.shadowolf.config.PluginConfig;

final public class ConfigConsumer {
	final private static boolean DEBUG = true;
	final private static Logger LOGGER = Logger.getLogger(ConfigConsumer.class);

	@SuppressWarnings("unchecked")
	public static Plugin consume(final PluginConfig config) throws IllegalArgumentException, SecurityException, InstantiationException,
	IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		final Class<Plugin> clazz = (Class<Plugin>) Class.forName(config.getClassName());

		if(DEBUG) {
			LOGGER.debug("Instantiating: " + config.getClassName());
		}
		final Plugin p = clazz.getConstructor(new Class[] { Map.class })
		.newInstance(config.getParameters());

		if(p instanceof ScheduledPlugin) {
			((ScheduledPlugin) p).setInitialDelay(config.getDelay());
			((ScheduledPlugin) p).setPeriod(config.getPeriod());
			((ScheduledPlugin) p).setUnit(config.getTimeUnit());
		}

		return p;
	}
}
