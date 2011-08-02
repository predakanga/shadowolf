package com.shadowolf.plugin;

import java.net.URLClassLoader;

import com.google.common.collect.Multimap;

/**
 * Class that represents a plugin.
 *
 */
public class Plugin {
	private String name;
	private URLClassLoader classloader;
	private Multimap<Class<?>, Object> extensions;

	/**
	 * The plugin's name.
	 */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * The classloader used to load this plugin's classes.
	 */
	public URLClassLoader getClassloader() {
		return classloader;
	}
	public void setClassloader(URLClassLoader classloader) {
		this.classloader = classloader;
	}
	
	/**
	 * A Multimap with keys of the extension point interfaces
	 * and values of objects that implement those interfaces.
	 * An object may appear here more than once under several
	 * different keys.
	 */
	public Multimap<Class<?>, Object> getExtensions() {
		return extensions;
	}
	public void setExtensions(Multimap<Class<?>, Object> extensions) {
		this.extensions = extensions;
	}
	
	
}
