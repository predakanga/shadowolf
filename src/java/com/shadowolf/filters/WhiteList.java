package com.shadowolf.filters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.shadowolf.tracker.TrackerResponse;

public class WhiteList implements Filter {
	private static Logger LOGGER = Logger.getLogger(WhiteList.class);
	private WhitelistParser parser = new WhitelistParser();
	private long lastParsed = 0;
	private String listPath;
	private String[] list = new String[0];
	private ArrayList<String> tempList = new ArrayList<String>();
	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

	@Override
	public void init(FilterConfig conf) throws ServletException {
		if(System.getenv("com.shadowolf.filters.whitelist.path") != null) {
			this.listPath = System.getenv("com.shadowolf.whitelist.path");
		} else {
			this.listPath = conf.getServletContext().getRealPath("/WEB-INF/whitelist.xml");
		}
		
		this.executor.scheduleAtFixedRate(new ParserThread(), 0L, 600L, TimeUnit.SECONDS);
	}

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		//if the list is empty, it probably hasn't been parsed
		if(this.list.length == 0) {
			
			//since it's probably not parsed, we'll go ahead and manually parse it
			this.parseList();
			
			//if it's still empty, the list itself is empty, so we have to warn that and approve everything (denying all requests seems silly)
			if(this.list.length == 0) {
				LOGGER.warn("Whitelist is empty... approving all requests.");
				chain.doFilter(request, response);
				return;
			}
		}
		
		for(String peerId : this.list) {
			if(request.getParameter("peer_id").startsWith(peerId)) {
				chain.doFilter(request, response);
				return;
			}
		}
		
		response.getWriter().write(TrackerResponse.bencoded("Your client is banned."));
	}

	private void parseList() {
		SAXParser parser;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(this.listPath, this.parser);
		} catch (Exception e) {
			LOGGER.error("Could not parse whitelist file.");
			return;
		}
		
		synchronized(this.list) {
			this.list = this.tempList.toArray(new String[this.tempList.size()]);
			this.tempList = new ArrayList<String>(this.list.length);
		}
		
		this.lastParsed = (new Date()).getTime();
	}
	
	private boolean isModified() {
		return this.lastParsed < (new File(this.listPath)).lastModified();
	}
	
	private void addListElement(String peerId) {
		this.tempList.add(peerId);
	}
	
	private class WhitelistParser extends DefaultHandler {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if(qName.equalsIgnoreCase("peer_id")) {
				addListElement(attributes.getValue("name"));
			}
		}
			
	}
	
	private class ParserThread implements Runnable {
		@Override
		public void run() {
			if(isModified()) {
				parseList();
			}
		}		
	}
}
