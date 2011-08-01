package com.shadowolf.util.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactory that creates threads with the given name and a numerical prefix,
 * beginning with 0.
 */
public class NamedThreadFactory implements ThreadFactory {
	private final static AtomicInteger THREAD_POOL_NUM = new AtomicInteger(1);
	
	private final AtomicInteger threadNum = new AtomicInteger(1);
	private final ThreadGroup threadGroup;
	private final String name;

	/**
	 * Creates a ThreadFactory that names threads with name prefix <i>name</i>
	 * @param name The name prefix (will be appended a "-" and thread number
	 */
	public NamedThreadFactory(String name) {
		final SecurityManager s = System.getSecurityManager();
		final ThreadGroup group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
				.getThreadGroup();
		
		threadGroup = new ThreadGroup(group, name + "-" + THREAD_POOL_NUM.getAndIncrement());
		this.name = name;
	}

	/* non-javadoc */
	@Override
	public Thread newThread(Runnable r) {
		final Thread thread = new Thread(threadGroup, r, name + threadNum.getAndIncrement());
		thread.setDaemon(false);
		thread.setPriority(Thread.NORM_PRIORITY);
		return thread;
	}

}
