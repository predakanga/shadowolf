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
public class InfoHashCache extends DatabaseWrapper implements Runnable {
	private static final boolean DEBUG = true;
	private static final Logger LOGGER = Logger.getLogger(InfoHashCache.class);
	
	//SQL crap
	private final String infoHashColumn;
	private final String idColumn;
	private final String query;
	
	private IDCache cache;
	
	/**
	 * Constructs a new InfoHashCache.  The configuration values torrents.table, torrents.info_hash_column, 
	 * and torrents.id_column are used to construct a query that looks, quite simple like:
	 * SELECT infoHash, torrent_id FROM torrents.
	 * @param configValues Configuration values, usually from configInstance.getParameters().
	 */
	public InfoHashCache(Map<String, String> configValues) {
		//Go ahead and build a faux cache here, so that calls to other methods don't blow up.
		Map<String, Integer> emptyHashesMap = Collections.emptyMap();
		Map<Integer, String> emptyIntMap = Collections.emptyMap();
		this.cache = new IDCache(emptyHashesMap, emptyIntMap);
		
		String tableName = configValues.get("torrents.table");
		String infoHashColumn = configValues.get("torrents.info_hash_column");
		String idColumn = configValues.get("torrents.id_column");
		
		if(tableName == null) {
			throw new RuntimeException("Required config value torrents.table not set.");
		} 
		
		if(infoHashColumn == null) {
			throw new RuntimeException("Required config value torrents.info_hash_column not set.");
		} else {
			this.infoHashColumn = infoHashColumn;
		}
		
		if(idColumn == null) {
			throw new RuntimeException("Required config value torrents.id_column not set.");
		} else {
			this.idColumn = idColumn;
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT ");
		builder.append(infoHashColumn); 
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
			Map<String, Integer> torrents = new FastMap<String,Integer>(this.cache.size());
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
						torrents.put(hex, torrentId);
						idNumbers.put(torrentId, hex);
					} finally {
						infoHash.free();
					}
				}
				
				this.cache = new IDCache(torrents, idNumbers);
			} finally {
				stmt.close();
			}
		} catch(SQLException e) {
			LOGGER.error(Exceptions.logInfo(e));
			this.connectionIsValid = false;
		} finally {
			if(DEBUG) {
				LOGGER.debug("Finished InfoHashCache.run(). New cache size: " + this.cache.size());
			}
		}
	}
	
	/**
	 * Returns the torrent id specified by this infoHash or null if not present.
	 * @param infoHash the info_hash to lookup
	 * @return the torrent id, or null if not found.
	 */
	public Integer lookupTorrentId(byte[] infoHash) {
		return this.lookupTorrentId(Data.byteArrayToHexString(infoHash));
	}
	
	/**
	 * Returns the torrent id specified by this hex-encoded infoHash, or null if not present.
	 * @param hexHash the hex-encoded infoHash.
	 * @return the torrent ID (Integer object) or null.
	 */
	public Integer lookupTorrentId(String hexHash) {
		return this.cache.lookupTorrentId(hexHash);
	}
	
	/**
	 * Returns the hex-encoded infoHash for the given torrent ID or null if not present.
	 * @param torrentId the hex-encoded infoHash.
	 * @return the hex-encoded infoHash or null.
	 */
	public String lookupInfoHash(Integer torrentId) {
		return this.cache.lookupInfoHash(torrentId);
	}
	
	/**
	 * Removes the specified hex-encoded infoHash from this cache.
	 * @param hexHash the infoHash to remove
	 */
	public void removeByInfoHash(String hexHash) {
		this.cache.removeByInfoHash(hexHash);
	}
	
	/**
	 * Removes the specified infoHash from this cache.
	 * @param infoHash the infoHash to remove.
	 */
	public void removeByInfoHash(byte[] infoHash) {
		this.removeByInfoHash(Data.byteArrayToHexString(infoHash));
	}
	
	/**
	 * Removes the specified torrent ID from this cache.  This method is n
	 * @param torrentId the torrent ID to remove.
	 */
	public void removeByTorrentId(Integer torrentId) {
		this.cache.removeByTorrentId(torrentId);
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
		public Map<String, Integer> infoHashes;
		public Map<Integer, String> idNumbers;
		
		private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
		private final Lock readLock = readWriteLock.readLock();
		private final Lock writeLock = readWriteLock.writeLock();
		
		public IDCache(Map<String, Integer> infoHashes, Map<Integer, String> idNumbers) {
			if(infoHashes.size() != idNumbers.size()) {
				throw new RuntimeException("IDCache instantiated with non-congruent sized maps.");
			}
			
			this.infoHashes = infoHashes;
			this.idNumbers = idNumbers;
		}
		
		public int size() {
			return this.infoHashes.size();
		}

		public Integer lookupTorrentId(String hexHash) {
			try {
				readLock.lock();
				return this.infoHashes.get(hexHash);
			} finally {
				readLock.unlock();
			}
		}
		
		public String lookupInfoHash(Integer torrentId) {
			try {
				readLock.lock();
				return this.idNumbers.get(torrentId);
			} finally {
				readLock.unlock();
			}
		}
		
		public void removeByInfoHash(String hexHash) {
			Integer torrentId = this.lookupTorrentId(hexHash);
			if(torrentId != null) {
				try {
					writeLock.lock();
					this.infoHashes.remove(hexHash);
					this.idNumbers.remove(torrentId);
				} finally {
					writeLock.unlock();
				}
			}
		}
		
		public void removeByTorrentId(Integer torrentId) {
			String hexHash = this.lookupInfoHash(torrentId);
			
			if(hexHash != null) {
				try {
					writeLock.lock();
					this.idNumbers.remove(torrentId);
					this.infoHashes.remove(hexHash);
				} finally {
					writeLock.lock();
				}
			}
			
		}
	}
}
