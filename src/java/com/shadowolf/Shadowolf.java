package com.shadowolf;

import java.io.File;
import java.net.InetAddress;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.shadowolf.server.AccessList;
import com.shadowolf.server.JettyServer;

public class Shadowolf {

	private static Configuration conf;
	
	public static Configuration getConfiguration() {
		try {
			if(conf == null) {
				conf = new PropertiesConfiguration("conf/shadowolf.properties");
			}
			return conf;
		} catch (ConfigurationException e) {
			throw new RuntimeException("Could not load application configuration");
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		String hostname = getConfiguration().getString("server.listen.address", "127.0.0.1");
		int port = getConfiguration().getInt("server.listen.port", 80);
		
		JettyServer server = new JettyServer(InetAddress.getByName(hostname), port);
		
		String conf = "conf/serverAccess.xml";
		JAXBContext context = JAXBContext.newInstance(AccessList.class);
		Unmarshaller um = context.createUnmarshaller();
		AccessList al = (AccessList) um.unmarshal(new File(conf));
		
		
		Logger.getLogger(Shadowolf.class).info("TEST1");
		LoggerFactory.getLogger(Shadowolf.class).info("TEST2");
		
		server.startServer(al);
	}

}
