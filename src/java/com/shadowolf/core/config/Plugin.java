package com.shadowolf.core.config;

import java.util.Collections;
import java.util.Map;

import javolution.util.FastMap;
import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.stream.XMLStreamException;

/**
 * Encapsulates the values from a &lt;plugin&gt; specified in config.xml.  See the xml file comments for more information on its formation.
 */
public class Plugin implements XMLSerializable {
	private static final long serialVersionUID = 1801664393544220873L;
	private String className;
	private Map<String, String> options = new FastMap<String, String>();
	
	private void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * Get the class name of this plugin.
	 * @return the name of the plugin's class.
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Returns an unmodifiable mapping of the options specified.  These are dealt with exactly as parameter tags are, see {@link Config#getParameters()} for more information. 
	 * @return the options map.
	 */
	public Map<String, String> getOptions() {
		return Collections.unmodifiableMap(options);
	}
	
	/**
	 * Returns an option with the specified name.
	 * @param key the name of the option
	 * @return the option value
	 */
	public String getOption(String key) {
		return this.options.get(key);
	}
	
	private void setOption(String key, String value) {
		this.options.put(key, value);
	}
	
	protected static XMLFormat<Plugin> PLUGIN_XML = new XMLFormat<Plugin>(Plugin.class) {

		@Override
		public void read(XMLFormat.InputElement xml, Plugin obj) throws XMLStreamException {
			obj.setClassName(xml.getAttribute("class").toString());
			
			Parameter p = xml.get("option", Parameter.class);
			
			while(p != null) {
				obj.setOption(p.getName(), p.getText());
				p = xml.get("option", Parameter.class);
			}
		}

		@Override
		public void write(Plugin obj, XMLFormat.OutputElement xml) throws XMLStreamException {
			
		}
		
	};
}
