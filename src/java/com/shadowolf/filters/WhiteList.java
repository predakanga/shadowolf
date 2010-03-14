package com.shadowolf.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.shadowolf.filters.common.XMLParsingFilter;
import com.shadowolf.tracker.TrackerResponse;

public class WhiteList extends XMLParsingFilter {
	private String[] list = new String[0];
	private static Logger LOGGER = Logger.getLogger(WhiteList.class);
	private ArrayList<String> tempList = new ArrayList<String>(0);
	
	
	@Override
	public void parse() {
		super.parse();
		
		synchronized(this.list) {
			this.list = this.tempList.toArray(new String[this.tempList.size()]);
			this.tempList = new ArrayList<String>(this.list.length);
		}
		
		this.lastParsed = (new Date()).getTime();
	}
	
	@Override
	protected XMLParser getParser() {
		return new WhiteListParser();
	}

	@Override
	protected String getPath() {	
		if(System.getenv("com.shadowolf.filters.whitelist.path") != null) {
			return System.getenv("com.shadowolf.whitelist.path");
		} else {
			return conf.getServletContext().getRealPath("/WEB-INF/whitelist.xml");
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		//if the list is empty, it probably hasn't been parsed
		if(this.list.length == 0) {
			
			//since it's probably not parsed, we'll go ahead and manually parse it
			this.parse();
			
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
	
	private void addListElement(String peerId) {
		this.tempList.add(peerId);
	}
	
	protected class WhiteListParser extends XMLParser {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if(qName.equalsIgnoreCase("peer_id")) {
				addListElement(attributes.getValue("name"));
			}
		}
	}

	@Override
	public void destroy() {}
}