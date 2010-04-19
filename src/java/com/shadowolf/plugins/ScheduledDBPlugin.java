package com.shadowolf.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.shadowolf.util.Exceptions;

abstract public class ScheduledDBPlugin extends ScheduledPlugin {
	private final static Logger LOGGER = Logger.getLogger(ScheduledDBPlugin.class);
	private final boolean DEBUG = false;

	protected Connection connection;

	protected void openConnection() {
		if(this.connection == null) {
			try {
				final DataSource source = (DataSource)
				(new InitialContext().lookup("java:comp/env/jdbc/shadowolf_db"));

				this.connection = source.getConnection();
				this.connection.setAutoCommit(false);
			} catch (final NamingException e) {
				LOGGER.error("Naming FATAL ERROR: " + e.getMessage());
			} catch (final SQLException e) {
				LOGGER.error("SQL FATAL ERROR: " + e.getMessage());
			}

		}
	}

	protected boolean rollback() {
		try {
			if(this.connection != null) {
				this.connection.rollback();
				this.connection.close();
				this.connection = null;
				return true;
			} else {
				return false;
			}
		} catch (final SQLException e) {
			try {
				this.connection.close();
			} catch(final SQLException ex) {
			}

			this.connection = null;

			return false;
		}
	}

	protected boolean commit() {
		try {
			if(this.connection != null) {
				this.connection.commit();
				return true;
			} else {
				return false;
			}
		} catch (final SQLException e) {
			try {
				this.connection.close();
			} catch(final SQLException ex) {
			}

			this.connection = null;

			return false;

		}
	}
	
	protected void ensureOpenConnection() {
		if(this.connection == null) {
			this.openConnection();
		} 

		try {
			this.connection.createStatement().execute("SELECT 1");
		} catch (final SQLException e) {
			try {
				this.connection.close();
			} catch(final SQLException ex) {
			}

			this.connection = null;

			this.openConnection();
		}

	}

	protected PreparedStatement prepareStatement(final String stmt) {
		if(this.DEBUG) {
			LOGGER.debug("Opening connection...");
		}


		this.ensureOpenConnection();

		if(this.DEBUG) {
			LOGGER.debug("Opened connection.");
		}

		try {
			return this.connection.prepareStatement(stmt);
		} catch (final SQLException e) {
			try {
				this.connection.close();
			} catch(final SQLException ex) {
				LOGGER.error(Exceptions.logInfo(ex));
			}

			this.connection = null;

			LOGGER.error(Exceptions.logInfo(e));
			return null;
		}
	}
}
