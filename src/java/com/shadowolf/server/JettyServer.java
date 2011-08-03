package com.shadowolf.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.security.Constraint;
import org.eclipse.jetty.http.security.Password;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shadowolf.ShadowolfComponent;
import com.shadowolf.ShadowolfContext;
import com.shadowolf.server.request.AnnounceRequest;
import com.shadowolf.server.request.ScrapeRequest;

/**
 * A Jetty server for shadowolf.  This is, for now, the only 
 * implementation.
 */
public class JettyServer implements ShadowolfComponent {
	private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);
	private static final String ANNOUNCE_URL = "/announce";
	private static final String SCRAPE_URL = "/scrape";
	
	private final Handler handler;
	private final InetAddress address;
	private final int port;
	private Server server;
	private ExecutorService executor = Executors.newCachedThreadPool(new ServerThreadFactory());
	
	private ShadowolfContext context;
	
	public JettyServer(InetAddress address, int port) throws ServletException {
		this.port = port;
		this.address = address;
		this.handler = new RequestHandler();
	}
	
	@Override
	public void setContext(ShadowolfContext context) {
		this.context = context;
	}

	@Override
	public ShadowolfContext getContext() {
		return context;
	}
	
	/**
	 * @return The executor service used to execute requests.
	 */
	public ExecutorService getExecutor() {
		return executor;
	}

	/**
	 * Set the executor service to be used to execute requests.
	 * @param executor
	 */
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	/**
	 * Start the server using the provided AccessList to dictate access rules.
	 * @param list
	 * @throws Exception
	 */
	public void startServer(AccessList accessList) throws Exception {
		LOG.info("Starting embedded Jetty server on " + this.address.getHostAddress() + ":" + this.port);
		InetSocketAddress address = new InetSocketAddress(this.address, port);
		this.server = new Server(address);
		this.server.setSendServerVersion(false);
		this.server.setSendDateHeader(false);
		this.server.setGracefulShutdown(30000);
		
		HashLoginService loginService = new HashLoginService();
		List<ConstraintMapping> maps = new ArrayList<ConstraintMapping>();
		
		if(accessList != null) {
			for(AccessList.Role role : accessList.getRoles()) {
				Constraint constraint = new Constraint();
				constraint.setName(Constraint.__BASIC_AUTH);
				constraint.setRoles(new String[] { role.getName() });
				constraint.setAuthenticate(true);
				
				for(String pathSpec : role.getUrls()) {
					LOG.info("Adding constraint for role: " + role.getName() + " at URL: " + pathSpec);
					ConstraintMapping mapping = new ConstraintMapping();
					mapping.setConstraint(constraint);
					mapping.setPathSpec(pathSpec);
					maps.add(mapping);
				}
				
			}
			
			for(AccessList.User u : accessList.getUsers()) {
				
				String[] roles = new String[u.getRoles().size()];
				for(int i = 0; i < u.getRoles().size(); i++) {
					roles[i] = u.getRoles().get(i).getName();
				}
				loginService.putUser(u.getUsername(), new Password(u.getPassword()), roles);
			}
		}
				

		ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
		csh.setConstraintMappings(maps);
		csh.setHandler(handler);
		csh.setLoginService(loginService);
		
		
		this.server.setHandler(csh);
		ThreadPool pool = new ExecutorThreadPool(this.executor);
		this.server.setThreadPool(pool);
		
		this.server.start();
		LOG.info("Server started");
	}
	
	/**
	 * Shut down the server
	 * @throws Exception
	 */
	public void shutdownServer() throws Exception { 
		this.server.stop();
	}
	
	/**
	 * Join the server to the calling thread.
	 * @throws InterruptedException
	 */
	public void join() throws InterruptedException { 
		this.server.join();
	}
	
	
	private static final class RequestHandler extends AbstractHandler implements Server.Graceful {
		private volatile boolean shutdown = false;
		
		private final HttpServlet announce = new AnnounceRequest();
		private final HttpServlet scrape = new ScrapeRequest();
		
		public RequestHandler() throws ServletException {
			super();
			announce.init();
			scrape.init();
		}
		
		
		@Override
		public void handle(String target, Request baseRequest, 
				HttpServletRequest request,HttpServletResponse response) 
				throws IOException, ServletException {
			if(shutdown || !request.getMethod().equalsIgnoreCase("get")) {
				return;
			}
			
			if(target.endsWith(ANNOUNCE_URL)) {
				if(target.length() > ANNOUNCE_URL.length()) {
					//the first character is always "/"
					baseRequest.getParameters().add("passkey", target.substring(1, target.length() - ANNOUNCE_URL.length()));
				} 
			}
			
			response.setContentType("text/plain;charset=utf-8");
			
			if(target.endsWith(ANNOUNCE_URL)) {
				response.setStatus(200);
				announce.service(request, response);
			} else if (target.endsWith(SCRAPE_URL)) {
				response.setStatus(200);
				scrape.service(request, response);
			} else {
				response.setStatus(404);
				final PrintWriter writer = response.getWriter();
				writer.print("404: not found.");
				writer.flush();
				writer.close();
			}
			
			baseRequest.setHandled(true);
			
		}

		@Override
		public void setShutdown(boolean shutdown) {
			this.shutdown = shutdown;
		}
	}

}
