package com.shadowolf.filters.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

abstract public class SingleColumnScheduledDatabaseFilter extends XMLParsingFilter {
	protected final static Logger LOGGER = Logger.getLogger(SingleColumnScheduledDatabaseFilter.class);
	protected final static String CONF_KEY = "com.shadowolf.sqlconf.path";
	protected final static String DEFAULT_CONF_PATH = "/WEB-INF/sqlconf.xml";
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	protected final static int DEFAULT_UPDATE = 15;
	protected ConcurrentSkipListSet<String> hashes = new ConcurrentSkipListSet<String>();

	protected String table;
	protected String column;
	protected ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

	protected abstract void parseResults(ResultSet rs);
	protected abstract String getSourceName();
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		super.init(config);

		this.executor.scheduleAtFixedRate(new SQLReader(), 0, DEFAULT_UPDATE, TimeUnit.SECONDS);
	}
	
	@Override
	protected XMLParser getParser() {
		return new SQLConfParser();
	}

	@Override
	protected String getPath() {
		if (System.getenv(CONF_KEY) != null) {
			return System.getenv(CONF_KEY);
		} else {
			return this.conf.getServletContext().getRealPath(DEFAULT_CONF_PATH);
		}
	}

	@Override
	public void destroy() {
		this.executor.shutdown();
	}

	protected class SQLConfParser extends XMLParser {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.equalsIgnoreCase("source") && attributes.getValue("name").equals(getSourceName())) {
				table = attributes.getValue("table");
				column = attributes.getValue("column");
			}
		}
	}

	protected class SQLReader implements Runnable {
		protected PreparedStatement stmt;

		public SQLReader() {
			try {
				DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
				Connection conn = source.getConnection();
				this.stmt = conn.prepareStatement("SELECT " + column + " FROM " + table);
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
				parseResults(this.stmt.getResultSet());
			} catch (SQLException e) {
				LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());

			}
		}
	}

}
