package com.shadowolf.plugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.shadowolf.plugin.points.AnnounceDecorator;
import com.shadowolf.plugin.points.AsyncAnnounceTask;
import com.shadowolf.plugin.points.LifeCycleTask;
import com.shadowolf.plugin.points.PreAnnounceFilter;
import com.shadowolf.plugin.points.ScheduledTask;
import com.shadowolf.protocol.Announce;

/**
 * Plugin Engine that serves as the public interface to the plugin engine.
 * 
 * <br/><br/>
 * 
 * Plugins added to a plugin engine instance are assumed to be properly
 * loaded, have their dependencies resolved, etc.  This should be taken care 
 * of by {@link PluginLoader} under normal circumstances.
 * 
 * <br/><br/>
 * 
 * This class encapsulates three separate runtime "ideas":
 * 
 * <ul><li>Scheduled Tasks</li>
 * 		<li>Async Announce Tasks</li>
 * 		<li>Immediate Tasks (LifeCycle, Filter, Decorator)</li></ul>
 * 
 * The scheduled tasks are executed in their own fixed-core-pool-size thread pool
 * that is created when the scheduler is started.  The scheduler can be started
 * one and only exactly one time.  Subsequent calls to startScheduler() will 
 * fizzle without having any effect on anything.
 *  
 * <strong>Every scheduled task is guaranteed exactly one thread</strong>.
 * 
 * <br/><br/>
 * Asynchronous tasks ({@link AsyncAnnounceTask}) are run in a separate thread pool
 * that is provided at construction time.
 * 
 * <br/><br/>
 * 
 * Immediate tasks ({@link LifeCycleTask}, {@link PreAnnounceFilter}, {@Link AnnounceDecorator}) run
 * in the calling thread.  This is because the majority of tasks are done in
 * http worker threads that would otherwise have to block for their result.
 * 
 */
public class PluginEngine {
	private Multimap<Class<?>, Object> objectsAtPoint = HashMultimap.create();
	private ScheduledThreadPoolExecutor scheduler;
	private ExecutorService asyncExecutor;
	
	/**
	 * Create a new PluginEngine with a default thread pool. 
	 * 
	 * @see Executors#newCachedThreadPool()
	 */
	public PluginEngine() {
		this(Executors.newCachedThreadPool());
	}
	
	/**
	 * Create a new PluginEngine with the provided executor service.
	 * @param asyncExecutor
	 */
	public PluginEngine(ExecutorService asyncExecutor) {
		this.asyncExecutor = asyncExecutor;
		
	}
	
	/**
	 * Invoke all register announce decorators on the provided 
	 * Announce object.
	 * @param announce the Announce to decorate
	 * @return the newly-decorated Announce.  Fabulous!
	 */
	public Announce invokeAnnounceDecorators(Announce announce) {
		for(Object plugin : objectsAtPoint.get(AnnounceDecorator.class)) {
			AnnounceDecorator decorator = (AnnounceDecorator) plugin;
			announce = decorator.decorate(announce);
		}
		
		return announce;
	}
	
	/**
	 * Queue an announce to be run against all provided 
	 * AsyncAnnounceTasks.
	 * 
	 * @param announce The Announce to provide to the async tasks.
	 */
	public void invokeAsyncAnnounceTasks(Announce announce) {
		for(Object plugin : objectsAtPoint.get(AsyncAnnounceTask.class)) {
			AsyncAnnounceTask task = (AsyncAnnounceTask) plugin;
			asyncExecutor.submit(new AsyncRunnable(announce, task));
		}
	}

	/**
	 * Invoke all the life cycle listeners' startup method.
	 */
	public void invokeLifeCycleTasksAtStartup() {
		for(Object plugin : objectsAtPoint.get(LifeCycleTask.class)) {
			LifeCycleTask task = (LifeCycleTask) plugin;
			task.onStart();
		}
	}
	
	/** 
	 * Invoke all the life cycle listeners' shutdown method 
	 */
	public void invokeLifeCycleTasksAtShutdown() {
		for(Object plugin : objectsAtPoint.get(LifeCycleTask.class)) {
			LifeCycleTask task = (LifeCycleTask) plugin;
			task.onShutdown();
		}
	}
	
	/**
	 * Run the announce through all provided Announce Filters until
	 * it either fails filtration or passes all filters.
	 * 
	 * @param announce The announce to filter
	 * @return whether the announce passed filtration or not
	 */
	public boolean invokePreAnnounceFilters(Announce announce) {
		for(Object plugin : objectsAtPoint.get(PreAnnounceFilter.class)) {
			PreAnnounceFilter filter = (PreAnnounceFilter)plugin;
			
			if(!filter.filter(announce)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Add an announce decorator to the engine.
	 */
	public void addAnnounceDecorator(AnnounceDecorator decorator) {
		objectsAtPoint.put(AnnounceDecorator.class, decorator);
	}
	
	/**
	 * Add an AsyncAnnounceTask to the engine
	 */
	public void addAsyncAnnounceTask(AsyncAnnounceTask task) {
		objectsAtPoint.put(AsyncAnnounceTask.class, task);
	}

	/**
	 * Add a LifeCycleTask to the engine.
	 */
	public void addLifeCycleTask(LifeCycleTask task) {
		objectsAtPoint.put(LifeCycleTask.class, task);
	}

	/** 
	 * Add an AnnounceFilter to the engine.
	 */
	public void addPreAnnounceFilter(PreAnnounceFilter filter) {
		objectsAtPoint.put(PreAnnounceFilter.class, filter);
	}

	/**
	 * Add a ScheduledTask to the engine.
	 */
	public void addScheduledTask(ScheduledTask task) {
		objectsAtPoint.put(ScheduledTask.class, task);
	}

	/**
	 * Starts the scheduler.  This should only ever be called one time.
	 * Subsequent calls accomplish nothing!
	 */
	public void startScheduler() {
		int size = objectsAtPoint.get(ScheduledThreadPoolExecutor.class).size();
		
		if(size > 0 && scheduler == null) {
			scheduler = new ScheduledThreadPoolExecutor(size);
			
			for(Object plugin : objectsAtPoint.get(ScheduledTask.class)) {
				ScheduledTask task = (ScheduledTask) plugin;
				
				scheduler.scheduleAtFixedRate(task, task.getDelay(), task.getInterval(), task.getTimeUnit());
			}
		}
		
	}
		
	/**
	 * Add a plugin to the PluginEngine.  This is equivalent
	 * to removing all the extensions from the Plugin and manually
	 * adding them.
	 */
	public void addPlugin(Plugin plugin) {
		Multimap<Class<?>, Object> extensions = plugin.getExtensions();
		
		for(Object o : extensions.get(AnnounceDecorator.class)) {
			addAnnounceDecorator((AnnounceDecorator) o);
		}
		
		for(Object o : extensions.get(AsyncAnnounceTask.class)) {
			addAsyncAnnounceTask((AsyncAnnounceTask) o);
		}
		
		for(Object o : extensions.get(LifeCycleTask.class)) {
			addLifeCycleTask((LifeCycleTask) o);
		}
		
		for(Object o : extensions.get(PreAnnounceFilter.class)) {
			addPreAnnounceFilter((PreAnnounceFilter) o);
		}
		
		for(Object o : extensions.get(ScheduledTask.class)) {
			addScheduledTask((ScheduledTask) o);
		}
	}
	
	
	private static class AsyncRunnable implements Runnable {
		private Announce announce;
		private AsyncAnnounceTask task;
		
		public AsyncRunnable(Announce announce, AsyncAnnounceTask task) {
			this.announce = announce;
			this.task = task;
		}
		
		@Override
		public void run() {
			task.run(announce);
		}
		
	}
}
