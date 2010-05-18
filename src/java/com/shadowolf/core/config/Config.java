package com.shadowolf.core.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.xml.XMLFormat;
import javolution.xml.XMLObjectReader;
import javolution.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import com.shadowolf.util.Exceptions;

/**
 * Access to configuration values specified in (by default) WEB-INF/config.xml.  See the XML file for more detailed comments on the schema for that file.
 */
public class Config {
	private static Logger LOGGER = Logger.getLogger(Config.class);
	
	private Map<String, String> parameters = new FastMap<String, String>();
	private Set<Plugin> plugins = new FastSet<Plugin>();

	/**
	 * Constructs a new instance of this class with the given path to the configuration file. 
	 * @param path the path to the configuration file
	 * @return the newly created Config instance
	 */
	public static Config newInstance(String path) {
		try {
			FileInputStream file = new FileInputStream(path);
			XMLObjectReader reader = XMLObjectReader.newInstance(file);
			
			return reader.read("tracker", Config.class);
		} catch (FileNotFoundException e) {
			LOGGER.error(Exceptions.logInfo(e));
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			LOGGER.error(Exceptions.logInfo(e));
			throw new RuntimeException(e);
		}
	}
	
	private void setParameter(String key, String value) {
		this.parameters.put(key, value.trim());
	}
	
	private void addPlugin(Plugin p) {
		this.plugins.add(p);
	}
	
	/**
	 * Returns a map of all parameters, maps to the &lt;parameter&gt; tag. The keys are the tag's attribute "name" and the values are the values of the tags themselves.
	 * <br />
	 * For example, this tag: &lt;parameter name="foo"&gt;bar&lt;parameter&gt; would be an entry with the key "foo" and the value "bar" in this map.
	 * @return the Parameter map.
	 */
	public Map<String, String> getParameters() {
		return Collections.unmodifiableMap(this.parameters);
	}
	
	/**
	 * Returns the value for the specified parameter. See {@link Config#getParameter(String)} for more details.
	 * @param key the name of the parameter
	 * @return the value of the parameter
	 */
	public String getParameter(String key) {
		return this.parameters.get(key);
	}
	
	/**
	 * Returns a set of the {@link Plugin} configuration instances.
	 * @see {@link Plugin}
	 * @return the plugin set
	 */
	public Set<Plugin> getPlugins() {
		return Collections.unmodifiableSet(this.plugins);
	}
	
	protected static XMLFormat<Config> CONFIG_XML = new XMLFormat<Config>(Config.class) {

		@Override
		public void read(XMLFormat.InputElement xml, Config config) throws XMLStreamException {
			Parameter p = xml.get("parameter", Parameter.class);
			while(p != null) {
				config.setParameter(p.getName(), p.getText());
				p = xml.get("parameter", Parameter.class);
			}
			
			Plugin plugin = xml.get("plugin", Plugin.class);
			
			while(plugin != null) {
				config.addPlugin(plugin);
				plugin = xml.get("plugin", Plugin.class);
			}
		}

		@Override
		public void write(Config config, XMLFormat.OutputElement xml) throws XMLStreamException {
			// TODO build me
			
		}
		
	};
	
	public static void main(String[] args) {
		Config c = Config.newInstance("C:/Users/Eddie/workspace/Shadowolf Refactor/WebContent/WEB-INF/config.xml");
		
		for(String key : c.getParameters().keySet()) {
			System.out.println("Parameter: " + key + "; " + c.getParameter(key));
		}
		
		for(Plugin p : c.getPlugins()) {
			System.out.println("Plugin: " + p.getClassName());
			for(String key : p.getOptions().keySet()) {
				System.out.println("Parameter: " + key + "; " + p.getOption(key));
			}
			
		}
	}

}
