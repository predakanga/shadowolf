package com.shadowolf.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.shadowolf.util.Exceptions;

/**
 * Hides a lot of logic that's necessary (and repetitive) to talk to MySQL, such
 * as ensuring an open connection when preparing statements.  This class sets
 * auto committing to false; if you want to change this behavior, override openConnection().
 * 
 * <br/><br/>
 * 
 * The standard practice in SW code is to wrap database interaction in a separate class for
 * a multitude of obvious reasons. 
 */
abstract public class DatabaseWrapper {
	private final static Logger LOGGER = Logger.getLogger(DatabaseWrapper.class);
	//private final boolean DEBUG = false;

	/**
	 * The connection to the database.
	 */
	protected Connection connection;
	
	/**
	 * Whether the connection to the database is valid.
	 * <strong>YOU MUST MANUALLY SET THIS TO FALSE IF YOU CATCH A SQLEXCEPTION</strong>
	 */
	protected boolean connectionIsValid = false;
	
	/**
	 * Opens a connection to the database.  This can be called repeatedly as this class
	 * keeps track of the validity of its connection to the database.
	 */
	protected void openConnection() {
		if(this.connection == null || !this.connectionIsValid) {
			try {
				final DataSource source = (DataSource)
						(new InitialContext().lookup("java:/comp/env/jdbc/shadowolf_db"));

				this.connection = source.getConnection();
				this.connection.setAutoCommit(false);
			} catch (final NamingException e) {
				LOGGER.error(Exceptions.logInfo(e));
				throw new RuntimeException("Could not connect to MySQL", e);
			} catch (final SQLException e) {
				LOGGER.error(Exceptions.logInfo(e));
				this.connectionIsValid = false;
				throw new RuntimeException("Could not connect to MySQL", e);
			}

		}
	}
	
	/**
	 * Commits a transaction.
	 * @return the success of the commit.
	 */
	protected boolean commit() {
		try {
			if(this.connectionIsValid) {
				this.connection.commit();
				return true;
			} else {
				return false;
			}
		} catch (final SQLException e) {
			try {
				this.connection.close();
			} catch(final SQLException ex) {
				LOGGER.error(Exceptions.logInfo(ex));
			} finally {
				LOGGER.error(Exceptions.logInfo(e));
				this.connectionIsValid = false;
			}
			
			return false;
		}
	}
	
	/**
	 * Rolls back a transaction and closes the connection. 
	 * @return the success of the rollback.
	 */
	protected boolean rollback() {
		try {
			if(this.connectionIsValid) {
				this.connection.rollback();
				this.connection.close();
				return true;
			} else {
				return false;
			}
		} catch (final SQLException e) {
			try {
				this.connection.close();
			} catch(final SQLException ex) {
				LOGGER.error(Exceptions.logInfo(ex));
			} finally {
				LOGGER.error(Exceptions.logInfo(e));
				this.connectionIsValid = false;
			}
			return false;
		}
	}
	
	/**
	 * Prepares a statement, after ensuring an open connection.
	 * @param stmt the statement to prepare.
	 * @return the prepared statement.
	 */
	protected PreparedStatement prepareStatement(final String stmt) {
		this.openConnection();

		try {
			return this.connection.prepareStatement(stmt);
		} catch (final SQLException e) {
			try {
				this.connection.close();
			} catch(final SQLException ex) {
				LOGGER.error(Exceptions.logInfo(ex));
			} finally {
				LOGGER.error(Exceptions.logInfo(e));
				this.connectionIsValid = false;
			}
			return null;
		}
	}
	
	
}
