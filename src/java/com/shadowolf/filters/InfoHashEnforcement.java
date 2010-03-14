package com.shadowolf.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.xml.sax.Attributes;

import com.shadowolf.filters.common.XMLParsingFilter;

public class InfoHashEnforcement extends XMLParsingFilter {
	private String table;
	private String column;
	
	@Override
	protected XMLParser getParser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getPath() {
		if(System.getenv("com.shadowolf.filters.whitelist.path") != null) {
			return System.getenv("com.shadowolf.whitelist.path");
		} else {
			return this.conf.getServletContext().getRealPath("/WEB-INF/sqlconf.xml");
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

	}

	protected class SQLConfParser extends XMLParser {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if(qName.equalsIgnoreCase("source") && attributes.getValue("name") == "info_hash") {
				table = attributes.getValue("table");
				column = attributes.getValue("column");
			}
		}
	}

	@Override
	public void destroy() {}
}
