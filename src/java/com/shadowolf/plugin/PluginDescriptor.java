package com.shadowolf.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="plugin")
@XmlType(propOrder = {"name", "version", "author","dependencies", "extensions" })
public class PluginDescriptor {
	public static enum Type {
		AnnounceDecorator,
		AsyncAnnounceTask,
		LifeCycleTask,
		PreAnnounceFilter,
		ScheduledTask,
	};
	
	private String name;
	private String displayName;
	private String version;
	private String author;
	private List<String> dependencies;
	private List<Extension> extensions;
	

	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute 
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
	@XmlElement(name="dependency")
	public List<String> getDependencies() {
		if(dependencies == null) {
			dependencies = new ArrayList<>(0);
		}
		
		return dependencies;
	}
	public void setDependencies(List<String> dependency) {
		this.dependencies = dependency;
	}
	
	@XmlElement(name="extension")
	public List<Extension> getExtensions() {
		return extensions;
	}
	public void setExtensions(List<Extension> extensions) {
		this.extensions = extensions;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PluginDescriptor other = (PluginDescriptor) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public static class Extension {
		private PluginDescriptor.Type point;
		private String className;
		
		@XmlElement(name="point")
		public PluginDescriptor.Type getPoint() {
			return point;
		}
		public void setPoint(PluginDescriptor.Type point) {
			this.point = point;
		}
		
		@XmlElement(name="class")
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
		
		
	}
	
	public static void main(String[] args) throws Exception {
		JAXBContext context = JAXBContext.newInstance(PluginDescriptor.class);
		Unmarshaller um = context.createUnmarshaller();
		PluginDescriptor a = (PluginDescriptor) um.unmarshal(new File("src/plugin/ClientWhitelist/sw_plugin.xml"));

		System.out.println(a.getExtensions().get(0).point);
	}
}
