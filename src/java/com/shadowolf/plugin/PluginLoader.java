package com.shadowolf.plugin;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.shadowolf.plugin.PluginDescriptor.Extension;
import com.shadowolf.plugin.points.AnnounceDecorator;
import com.shadowolf.plugin.points.AsyncAnnounceTask;
import com.shadowolf.plugin.points.LifeCycleTask;
import com.shadowolf.plugin.points.PreAnnounceFilter;
import com.shadowolf.plugin.points.ScheduledTask;
import com.shadowolf.util.Exceptions;

/**
 * Utility class that contains methods designed
 * to facilitate the creation of {@link Plugin} instances from
 * their XML files.
 * 
 * <br/><br/>
 * 
 * The class also keeps track of all plugins that it's loaded
 * so that it only loads them once.
 */
public class PluginLoader {
	private final String pluginsFolder = "src/plugin/";
	private final String libsFolder = "src/lib";
	private final JAXBContext context;
	private final Unmarshaller unmarshaller;
	private final Map<String, Plugin> plugins = new HashMap<String, Plugin>();
	
	public PluginLoader() {
		try {
			context = JAXBContext.newInstance(PluginDescriptor.class);
			unmarshaller = context.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Return whether or not a plugin exists in the plugin directory,
	 * usually the "plugins/" directory.
	 * @param name the plugin to search for 
	 * @return whether the searched plugin is found
	 */
	public boolean pluginExists(String name) {
		File f = new File(pluginsFolder + name);
		return f.exists() && f.canRead() && f.isDirectory();
	}
	
	/**
	 * Creates a PluginDescriptor describing a plugin with name name. 
	 * @param name The name of the plugin
	 * @return a PluginDescriptor or null if the plugin cannot be found
	 */
	public PluginDescriptor createPluginDescriptor(String name) {
		if(!pluginExists(name)) {
			return null;
		}
		
		return createPluginDescriptor(new File(pluginsFolder + name + "/sw_plugin.xml"));
	}

	private PluginDescriptor createPluginDescriptor(File file) {
		try {
			if(file.exists() && file.canRead() && file.isDirectory()) {
				return (PluginDescriptor) unmarshaller.unmarshal(file); 
			} else {
				return null;
			}
		} catch (JAXBException e) {
			Exceptions.log(e);
			return null;
		}
	}
	
	/**
	 * Creates a plugin from a given PluginDescriptor.  The Plugin is then suitable
	 * for consumption by a PluginEngine.
	 * 
	 * <br/><br/>
	 * 
	 * This will not only load the given plugin, but also all of its dependencies.
	 * 
	 * @param descriptor The descriptor to create a plugin from
	 * @return The created plugin.
	 */
	public Plugin createPluginFromDescriptor(PluginDescriptor descriptor) {
		Plugin p;
		try {
			DependencyTree<PluginDescriptor> tree = loadTree(createPluginDescriptor("ClientWhiteList"));
			p = loadPluginRecurse(tree.getVal(), null);
		} catch (UnmetDependencyException | CircularDependencyException e) {
			Exceptions.log(e);
			p = new Plugin();
		}
		
		
		return p;
	}
	
	private Plugin loadPluginRecurse(PluginDescriptor descriptor, URLClassLoader parent) {
		if(plugins.containsKey(descriptor.getName())) {
			return plugins.get(descriptor.getName());
		}
		
		URLClassLoader loader = parent;
		
		for(String str : descriptor.getDependencies()) {
			PluginDescriptor desc = createPluginDescriptor(str);
			Plugin p = loadPluginRecurse(desc, loader);
			loader = p.getClassloader();
		}
		
		return loadPluginNoDeps(descriptor, loader);
	}
	
	private Plugin loadPluginNoDeps(PluginDescriptor descriptor, URLClassLoader parent) {
		Plugin p = new Plugin();
		
		String pluginDir = pluginsFolder + "/" + descriptor.getName();
		String libDir = pluginDir + "/" + libsFolder;
		
		File libs = new File(libDir);
		File[] jars = libs.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().endsWith(".jar");
			}
		});
		
		URL[] urls = new URL[jars.length + 1];
		
		for(int i = 0; i < jars.length; i++) {
			try {
				urls[i] = jars[i].toURI().toURL();
			} catch (MalformedURLException e) {
				Exceptions.log(e);
			}
		}
		
		try {
			urls[jars.length] = new URL("file:/" + new File(pluginDir).getAbsolutePath() + "/" + descriptor.getName() + ".jar");
		} catch (MalformedURLException e) {
			Exceptions.log(e);
		}

		URLClassLoader loader;
		if(parent != null) {
			loader = new URLClassLoader(urls, parent);
		} else {
			loader = new URLClassLoader(urls);
		}
		
		
		Multimap<Class<?>, Object> extensions = HashMultimap.create();
		Set<String> instantiated = new HashSet<>();
		
		for(Extension e : descriptor.getExtensions()) {
			String className = e.getClassName();

			if(instantiated.contains(className)) {
				continue;
			}
			
			try {
				Object o = loader.loadClass(className).newInstance();
				
				if(o instanceof AnnounceDecorator) {
					extensions.put(AnnounceDecorator.class, o);
				}
				
				if(o instanceof AsyncAnnounceTask) {
					extensions.put(AsyncAnnounceTask.class, o);
				}
				
				if(o instanceof LifeCycleTask) {
					extensions.put(LifeCycleTask.class, o);
				}
				
				if(o instanceof PreAnnounceFilter) {
					extensions.put(PreAnnounceFilter.class, o);
				}
				
				if(o instanceof ScheduledTask) {
					extensions.put(ScheduledTask.class, o);
				}
			
				instantiated.add(className);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
				Exceptions.log(ex);
				throw new RuntimeException("Error instantiating plugin, please see logs.");
			}
		}
		
		p.setExtensions(extensions);
		p.setClassloader(loader);
		p.setName(descriptor.getName());
		
		plugins.put(p.getName(), p);
		return p;
	}
	
	private DependencyTree<PluginDescriptor> loadTree(PluginDescriptor descriptor) throws UnmetDependencyException, CircularDependencyException {
		return loadTree(descriptor, null);
	}
	
	private DependencyTree<PluginDescriptor> loadTree(PluginDescriptor descriptor, DependencyTree<PluginDescriptor> parent) throws UnmetDependencyException, CircularDependencyException {
		DependencyTree<PluginDescriptor> tree = new DependencyTree<>(descriptor, parent);
		if(parent != null) {
			parent.addChild(tree);
		}
		
		if(descriptor.getDependencies() != null && descriptor.getDependencies().size() > 0) {
			for(String dependency : descriptor.getDependencies()) {
				if(!pluginExists(dependency)) {
					throw new UnmetDependencyException("Unable to meet dependency: " + dependency + " - it could not be found.");
				}
				
				PluginDescriptor depDescriptor = createPluginDescriptor(dependency);
				if(tree.hasAncestor(depDescriptor)){
					StringBuilder message = new StringBuilder()
							.append(depDescriptor.getName()) 
							.append(" was found to be in its own depedency tree.  ")
							.append(depDescriptor.getName())
							.append(" > ");
					
					
					DependencyTree<PluginDescriptor> par = tree;
					message.append(par.getVal().getName());
					
					while(par.hasParent()) {
						message.append(" > ").append(par.getParent().getVal().getName());
						par = par.getParent();
					}
					
					throw new CircularDependencyException(message.toString());
				}
				
				loadTree(depDescriptor, tree);
				
			}
		}
		
		return tree;
	}
}
