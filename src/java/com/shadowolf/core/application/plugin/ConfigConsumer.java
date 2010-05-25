package com.shadowolf.core.application.plugin;

import java.lang.reflect.Constructor;
import java.util.Map;

import javax.el.MethodNotFoundException;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.shadowolf.core.config.PluginConfig;
import com.shadowolf.util.Exceptions;

final class ConfigConsumer {
	final private static boolean DEBUG = true;
	final private static Logger LOGGER = Logger.getLogger(ConfigConsumer.class);
	
	@SuppressWarnings("unchecked")
	public static AbstractPlugin consume(final PluginConfig config, ServletContext context) {
		try {
			final Class<AbstractPlugin> clazz = (Class<AbstractPlugin>) Class.forName(config.getClassName());
			
			if(DEBUG) {
				LOGGER.debug("Instantiating plugin: " + clazz.getCanonicalName());
			}
			
			Constructor<? extends AbstractPlugin> constructor;
			
			try {
				constructor = clazz.getConstructor(new Class[] { Map.class, ServletContext.class });
				return  constructor.newInstance(config.getOptions(), context);
			} catch (MethodNotFoundException e) {
				constructor = clazz.getConstructor(new Class[] { Map.class });
				return  constructor.newInstance(config.getOptions());
			}
		} catch (Throwable e) {
			LOGGER.error(Exceptions.logInfo(e));
			throw new RuntimeException("There was a problem instantiating a plugin; see log for more details.");
		}
		
	}
}
