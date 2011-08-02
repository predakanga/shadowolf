package com.shadowolf.plugin;

import java.net.URLClassLoader;

import com.google.common.collect.Multimap;

public class Plugin {
	private String name;
	private URLClassLoader classloader;
	private Multimap<Class<?>, Object> extensions;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public URLClassLoader getClassloader() {
		return classloader;
	}
	public void setClassloader(URLClassLoader classloader) {
		this.classloader = classloader;
	}
	public Multimap<Class<?>, Object> getExtensions() {
		return extensions;
	}
	public void setExtensions(Multimap<Class<?>, Object> extensions) {
		this.extensions = extensions;
	}
	
	
}
