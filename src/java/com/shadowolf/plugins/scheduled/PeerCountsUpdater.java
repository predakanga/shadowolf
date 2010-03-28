package com.shadowolf.plugins.scheduled;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.shadowolf.plugins.ScheduledPlugin;
import com.shadowolf.user.PeerListFactory;

public class PeerCountsUpdater extends ScheduledPlugin {
	private final static boolean DEBUG = false;
	protected final static Logger LOGGER = Logger.getLogger(PeerCountsUpdater.class);
	protected final static String DATABASE_NAME = "java:comp/env/jdbc/database";
	
	private Connection conn;
	private PreparedStatement resetStmt;
	private PreparedStatement updateStmt;
	
	public PeerCountsUpdater(Attributes attributes) {
		super(attributes);
		
		final String table = attributes.getValue("table");
		final String seederC = attributes.getValue("seeder_column");
		final String leecherC = attributes.getValue("leecher_column");
		final String infoC = attributes.getValue("info_hash_column");
		try {
			final DataSource source = (DataSource) (new InitialContext().lookup(DATABASE_NAME));
			this.conn = source.getConnection();
			conn.setAutoCommit(false);
			
			this.resetStmt = this.conn.prepareStatement("UPDATE " + table + " SET " 
				+ seederC + "=0, " + leecherC + "=0");
			this.updateStmt = this.conn.prepareStatement("UPDATE " + table + " SET " 
				+ seederC + "= ? , " + leecherC + "= ? WHERE " + infoC + "= ?");
		} catch (NamingException n) {
			LOGGER.error("Unexpected NamingException...");
		} catch (SQLException e) {
			LOGGER.error("Unexpected SQLException..." + e.getMessage() + "\t Cause: " + e.getCause().getMessage());
		}
	}

	@Override
	public void run() {
		boolean failed = false;
		try {
			HashMap<byte[], int[]> updates = PeerListFactory.getPeerCounts();
			this.resetStmt.execute();
			if(DEBUG) {
				LOGGER.debug("Pushing stats for " + updates.size() + " torrents.");
			}
			
			Iterator<byte[]> iter = updates.keySet().iterator();
			while(iter.hasNext()) {
				byte[] next = iter.next();
				if(DEBUG) {
					LOGGER.debug("Updating torrent with Seeders: " + updates.get(next)[0]);
					LOGGER.debug("Updating torrent with Leechers: " + updates.get(next)[1]);
				}
				
				this.updateStmt.setInt(1, updates.get(next)[0]);
				this.updateStmt.setInt(2, updates.get(next)[1]);
				this.updateStmt.setBytes(3, next);
				this.updateStmt.execute();
				
				if(DEBUG) {
					LOGGER.debug("Updated torrent with Seeders: " + updates.get(next)[0]);
					LOGGER.debug("Updated torrent with Leechers: " + updates.get(next)[1]);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Failed query! " + e.getMessage());
			failed = true;
		} catch (Exception e) {
			e.printStackTrace();
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
	}

}
