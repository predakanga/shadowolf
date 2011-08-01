package com.shadowolf.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Special purpose wrapper around ThreadPoolExecutor.
 *
 */
public class ServerExecutorService extends ThreadPoolExecutor {
	/**
	 * Creates a new ThreadPoolExecutor for use with a Shadowolf server
	 * worker thread pool.
	 * 
	 * @param corePoolSize the minimum number of threads 
	 * @param maximumPoolSize the maximum number of threads
	 * @param workQueueSize the maximum amount of jobs that can be queued before they are rejected,
	 * 		at which point they will be handled by the calling thread.
	 */
	public ServerExecutorService(int corePoolSize, int maximumPoolSize, int workQueueSize) {
		super(corePoolSize, maximumPoolSize, 300, TimeUnit.SECONDS, 
				new LinkedBlockingQueue<Runnable>(workQueueSize),
				new ServerThreadFactory(), new CallerRunsPolicy());
	}
}
