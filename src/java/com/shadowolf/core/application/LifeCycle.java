package com.shadowolf.core.application;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.shadowolf.core.application.cache.InfoHashCache;
import com.shadowolf.core.config.Config;

/**
 * Listener class that encapsulates objects needed globally across the web application.
 * LifeCycle sets ServletContext attributes:
 * <ul>
 *	<li>"config": an instance of {@link Config} used across the application.</li>
 *	<li>"scheduledThreadPoolExecutor": an instance of a ScheduledThreadPoolExecutor.  
 *		All scheduled tasks <strong>should</strong> use this executor, but first they must increment
 *		the core pool size appropriately.
 *	</li>
 *	<li>"infoHashCache": an instance of {@link InfoHashCache}.  Anything needing to store
 *		identifying information about a torrent should use this instance and only this instance.
 * </ul>
 */
public class LifeCycle implements ServletContextListener {
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		Config config = Config.newInstance(context.getRealPath("/WEB-INF/config.xml"));
		InfoHashCache cache = new InfoHashCache(config.getParameters());
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		
		executor.scheduleAtFixedRate(cache, 0, Integer.parseInt(config.getParameter("torrents.cache_update_interval")), TimeUnit.SECONDS);

		context.setAttribute("config", config);
		context.setAttribute("infoHashCache", cache);
		context.setAttribute("scheduledThreadPoolExecutor", executor);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		
		ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) context.getAttribute("scheduledThreadPoolExecutor"); 
		executor.shutdown();
	}

}
