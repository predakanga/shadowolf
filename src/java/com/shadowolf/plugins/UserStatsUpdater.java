package com.shadowolf.plugins;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.shadowolf.user.User;
import com.shadowolf.user.UserFactory;

public class UserStatsUpdater implements Plugin {
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	protected final static Logger LOGGER = Logger.getLogger(UserStatsUpdater.class);
	protected final static String CONF_KEY = "com.shadowolf.sqlconf.path";
	protected final static String DEFAULT_CONF_PATH = "/WEB-INF/sqlconf.xml";
	
	protected boolean running = false;
	protected PreparedStatement stmt;
	protected String table;
	protected String downColumn;
	protected String upColumn;
	protected String passkeyColumn;
	protected Set<String> updates = new HashSet<String>();
	protected Connection conn;
	
	public UserStatsUpdater(ServletContext context) {
		LOGGER.debug("Instatiated UserStatsUpdater plugin.");
		this.updates = Collections.synchronizedSet(this.updates);
		
		try {
			String path;
			
			if (System.getenv(CONF_KEY) != null) {
				path = System.getenv(CONF_KEY);
			} else {
				path = context.getRealPath(DEFAULT_CONF_PATH);
			}
			
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(path, new XMLParser());
		} catch (ParserConfigurationException e) {
			LOGGER.error("Holy shit, nearly impossible ParserConfigurationException.");
			//this shouldn't ever happen
		} catch (SAXException e) {
			LOGGER.error("Holy shit, nearly impossible SAXException.");
			//neither should this
		} catch (IOException e) {
			LOGGER.error("Holy shit, nearly impossible IOException.");
			System.exit(69); //tee-hee
		}
		
		try {
			DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
			this.conn = source.getConnection();
			conn.setAutoCommit(false);
			this.stmt = conn.prepareStatement(
				"UPDATE " + table + " SET " + downColumn + "= " + downColumn + "+ ?, " 
				+ upColumn + "= " + upColumn + " + ? WHERE " + passkeyColumn + "=?");
		} catch (NamingException n) {
			LOGGER.error("Unexpected NamingException...");
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
	}
	
	public void addToUpdateQueue(String passkey) {
		synchronized(this.updates) {
			this.updates.add(passkey);
		}
	}
	
	@Override
	public void run() {
		LOGGER.debug("UserStatsUpdater plugin running.");
		if(this.running) {
			return;
		} else {
			this.running = true;
		}
		
		
		boolean failed = false;
		synchronized(this.updates) {
			int total = 0;
			
			if(this.updates.size() > 0) {
				total = (this.updates.size() > 6) ? this.updates.size() / 6 : this.updates.size();
			}
			
			if(total == 0) {
				this.running = false;
				return;
			}
			
			Iterator<String> iter = this.updates.iterator();
		
			for(int i = 0; failed == false && iter.hasNext() && i < total; i++) {
				String passkey = iter.next();
				
				User u = UserFactory.aggregate(passkey);
				this.updates.remove(passkey);
				
				Long down = u.getDownloaded();
				Long up = u.getUploaded();
				
				try {
					this.stmt.setLong(1, down);
					this.stmt.setLong(2, up);
					this.stmt.setString(3, passkey);
					this.stmt.executeUpdate();
				} catch (SQLException e) {
					failed = true;
				} 
			}
		}
		
		try {
			if(failed) {
				this.conn.rollback();
			} else {
				this.conn.commit();
			}
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}

		this.running = false;
	}

	protected class XMLParser extends DefaultHandler {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.equalsIgnoreCase("source") && attributes.getValue("name").equals("user_stats")) {
				table = attributes.getValue("table");
				upColumn = attributes.getValue("upload_column");
				downColumn = attributes.getValue("download_column");
			} else if (qName.equalsIgnoreCase("source") && attributes.getValue("name").equals("passkeys")) {
				passkeyColumn = attributes.getValue("column");
			}
		}
	}
}
