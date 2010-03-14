package com.shadowolf.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.shadowolf.filters.common.XMLParsingFilter;
import com.shadowolf.tracker.TrackerResponse;

public class InfoHashEnforcement extends XMLParsingFilter {
	private final static Logger LOGGER = Logger.getLogger(InfoHashEnforcement.class);
	private final static String CONF_KEY = "com.shadowolf.sqlconf.path";
	private final static String DEFAULT_CONF_PATH = "/WEB-INF/sqlconf.xml";
	private final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	
	private HashSet<String> hashes = new HashSet<String>();
	
	private String table;
	private String column;
	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		super.init(config);
		
		this.executor.scheduleAtFixedRate(new SQLReader(), 1, 30, TimeUnit.SECONDS);
	}
	
	
	@Override
	protected XMLParser getParser() {
		return new SQLConfParser();
	}

	@Override
	protected String getPath() {
		if(System.getenv(CONF_KEY) != null) {
			return System.getenv(CONF_KEY);
		} else {
			return this.conf.getServletContext().getRealPath(DEFAULT_CONF_PATH);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(hashes.contains(request.getParameter("info_hash"))) {
			chain.doFilter(request, response);
		} else {
			response.getWriter().write(TrackerResponse.Errors.TORRENT_NOT_REGISTERED.toString());
		}
	}

	@Override
	public void destroy() {
		this.executor.shutdown();
	}
	
	protected class SQLConfParser extends XMLParser {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if(qName.equalsIgnoreCase("source") && attributes.getValue("name").equals("info_hash")) {
				table = attributes.getValue("table");
				column = attributes.getValue("column");
			}
		}
	}
	
	private class SQLReader implements Runnable {
		private PreparedStatement stmt;
		
		public SQLReader() {
			try {
				DataSource source = (DataSource)(new InitialContext().lookup(DATABASE_NAME));
				Connection conn = source.getConnection();
				this.stmt = conn.prepareStatement("SELECT " + column + " FROM " + table);
				LOGGER.debug("SELECT " + column + " FROM " + table);
			} catch (NamingException n) {
				LOGGER.error("Unexpected NamingException...");
			} catch (SQLException e) {
				LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
			}
		}
		
		@Override
		public void run() {
			try {
				this.stmt.execute();
				final ResultSet rs = this.stmt.getResultSet();
				final HashSet<String> temp = new HashSet<String>(rs.getFetchSize());
				while(rs.next()) {
					temp.add(rs.getString(column));
				}
				
				synchronized(hashes) {
					hashes = temp;
				}
				
			} catch (SQLException e) {
				LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
			}
		}
	}
}
