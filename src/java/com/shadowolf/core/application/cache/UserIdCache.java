package com.shadowolf.core.application.cache;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.util.FastMap;

import org.apache.log4j.Logger;

import com.shadowolf.core.database.DatabaseWrapper;
import com.shadowolf.util.Data;
import com.shadowolf.util.Exceptions;

/**
 * Info hash cache represents a periodically updated info hash cache of torrents known in the database.
 * 
 * <br/>
 * 
 * The justification for this class is that, by storing the ID numbers and the infoHashes once, plugins
 * that need that data don't have to replicate it, but can store primitive ID numbers, instead, saving memory.
 * Also, WHERE clauses that bind binary data tend, in our experience, to behave inconsistently and this works
 * around that issue.
 */
public class UserIdCache extends DatabaseWrapper implements Runnable {
	private static final boolean DEBUG = true;
	private static final Logger LOGGER = Logger.getLogger(UserIdCache.class);
	
	//SQL crap
	private final String infoHashColumn;
	private final String idColumn;
	private final String query;
	
	private IDCache cache;
	
	/**
	 * Constructs a new UserIdCache.  The configuration values user.table, user.passkey_column, 
	 * and user.id_column are used to construct a query that looks, quite simple like:
	 * SELECT passkey, user_id FROM users.
	 * @param configValues Configuration values, usually from configInstance.getParameters().
	 */
	public UserIdCache(Map<String, String> configValues) {
		//Go ahead and build a faux cache here, so that calls to other methods don't blow up.
		Map<String, Integer> emptyHashesMap = Collections.emptyMap();
		Map<Integer, String> emptyIntMap = Collections.emptyMap();
		this.cache = new IDCache(emptyHashesMap, emptyIntMap);
		
		String tableName = configValues.get("user.table");
		String passkeyColumn = configValues.get("user.passkey_column");
		String idColumn = configValues.get("user.id_column");
		
		if(tableName == null) {
			throw new RuntimeException("Required config value user.table not set.");
		} 
		
		if(passkeyColumn == null) {
			throw new RuntimeException("Required config value user.info_hash_column not set.");
		} else {
			this.infoHashColumn = passkeyColumn;
		}
		
		if(idColumn == null) {
			throw new RuntimeException("Required config value user.id_column not set.");
		} else {
			this.idColumn = idColumn;
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT ");
		builder.append(passkeyColumn); 
		builder.append(", ");
		builder.append(idColumn);
		builder.append(" FROM ");
		builder.append(tableName);
		
		this.query = builder.toString();
		
		if(DEBUG) {
			LOGGER.debug("Prepared: " + this.query);
		}
		
	}
	
	/**
	 * Runs the database query, fetches the results and then swaps out the cache.
	 * <br/><br/>
	 * This is a build-and-swap option, to preserve threadsafety.  Rather than deal with
	 * the issues of thread-safety and torrents that don't belong in the cache forever, 
	 * we simply build another Map and then swap the references out.  Because java reference
	 * assignment is an atomic shared operation, no contention or dead references can exist.
	 * As a result of this methodology <strong>YOU MUST NOT HOLD REFERENCES TO THE INTERNAL
	 * MAP OUTSIDE OF THIS CLASS. DOING SO WILL RESULT IN HILARIOUSLY BAD MEMORY LEAKS</strong>.
	 * You have been warned.
	 */
	@Override
	public void run() {
		try {
			if(DEBUG) {
				LOGGER.debug("Starting to retrieve InfoHashCache.");
			}
			
			//we set the default value to the current size, since it'll be mostly appropriate
			//these are NOT marked shared() because the threadsafety is delegated to the inner class IDClass
			Map<String, Integer> users = new FastMap<String,Integer>(this.cache.size());
			Map<Integer, String> idNumbers = new FastMap<Integer,String>(this.cache.size());
			
			PreparedStatement stmt = this.prepareStatement(this.query);
			
			try {
				stmt.execute();
				ResultSet results = stmt.getResultSet();
				
				while(results.next()) {
					Blob infoHash = results.getBlob(this.infoHashColumn);
					
					try {
						String hex = Data.byteArrayToHexString(infoHash.getBytes(1L, (int)infoHash.length()));
						Integer torrentId = results.getInt(this.idColumn);
						users.put(hex, torrentId);
						idNumbers.put(torrentId, hex);
					} finally {
						infoHash.free();
					}
				}
				
				IDCache temp = new IDCache(users, idNumbers);
				this.cache = temp;
				if(DEBUG) {
					LOGGER.debug("Finished InfoHashCache.run(). New cache size: " + this.cache.size());
				}
			} finally {
				stmt.close();
			}
		} catch(SQLException e) {
			LOGGER.error(Exceptions.logInfo(e));
			this.connectionIsValid = false;
		} finally {
			
		}
		
		
	}
	
	/**
	 * Returns the torrent id specified by this passkey, or null if not present.
	 * @param passkey the passkey to look up.
	 * @return the user ID (Integer object) or null.
	 */
	public Integer lookupUserId(String passkey) {
		return this.cache.lookupUserId(passkey);
	}
	
	/**
	 * Returns the passkey for the given user ID or null if not present.
	 * @param userId the passkey.
	 * @return the passkey or null.
	 */
	public String lookupPasskey(Integer userId) {
		return this.cache.lookupPasskey(userId);
	}
	
	/**
	 * Removes the specified passkey from this cache.
	 * @param passkey the passkey to remove.
	 */
	public void removeByPasskey(String passkey) {
		this.cache.removeByPasskey(passkey);
	}
	
	/**
	 * Removes the specified user ID from this cache. 
	 * @param userId the user ID to remove.
	 */
	public void removeByUserId(Integer userId) {
		this.cache.removeByUserId(userId);
	}
	
	/**
	 * Get the size of this cache
	 */
	public int size() {
		return this.cache.size();
	}
	
	/**
	 * Simple class that wraps two maps so that the swap operation can be atomic, as well as removal and get operations.
	 */
	private class IDCache {
		public Map<String, Integer> passkeys;
		public Map<Integer, String> idNumbers;
		
		private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
		private final Lock readLock = readWriteLock.readLock();
		private final Lock writeLock = readWriteLock.writeLock();
		
		public IDCache(Map<String, Integer> passkeys, Map<Integer, String> idNumbers) {
			if(passkeys.size() != idNumbers.size()) {
				throw new RuntimeException("IDCache instantiated with non-congruent sized maps.");
			}
			
			this.passkeys = passkeys;
			this.idNumbers = idNumbers;
		}
		
		public int size() {
			return this.passkeys.size();
		}

		public Integer lookupUserId(String passkey) {
			try {
				readLock.lock();
				return this.passkeys.get(passkey);
			} finally {
				readLock.unlock();
			}
		}
		
		public String lookupPasskey(Integer torrentId) {
			try {
				readLock.lock();
				return this.idNumbers.get(torrentId);
			} finally {
				readLock.unlock();
			}
		}
		
		public void removeByPasskey(String passkey) {
			Integer userId = this.lookupUserId(passkey);
			if(userId != null) {
				try {
					writeLock.lock();
					this.passkeys.remove(passkey);
					this.idNumbers.remove(userId);
				} finally {
					writeLock.unlock();
				}
			}
		}
		
		public void removeByUserId(Integer userId) {
			String passkey = this.lookupPasskey(userId);
			
			if(passkey != null) {
				try {
					writeLock.lock();
					this.idNumbers.remove(userId);
					this.passkeys.remove(passkey);
				} finally {
					writeLock.lock();
				}
			}
			
		}
	}
}
