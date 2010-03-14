package com.shadowolf.filters.common;

import java.io.File;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

abstract public class XMLParsingFilter implements Filter {
	private static Logger LOGGER = Logger.getLogger(XMLParsingFilter.class);
	
	protected SAXParser saxParser = null;
	protected String path;
	protected long lastParsed = 0;
	protected FilterConfig conf;
	
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		try {
			this.saxParser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException e) {
			LOGGER.error("Holy shit, nearly impossible ParserConfigurationException.");
			//this shouldn't ever happen
		} catch (SAXException e) {
			LOGGER.error("Holy shit, nearly impossible SAXException.");
			//neither should this
		}
		
		this.conf = config;
		this.path = this.getPath();
		this.parse();
	}
	
	protected boolean isModified() {
		return this.lastParsed < (new File(this.path)).lastModified();
	}
	
	public void parse() {
		try {
			this.saxParser.parse(this.path, this.getParser());
		} catch (Exception e) {
			LOGGER.error("Could not parse whitelist file.");
			return;
		}
	}
	
	/*
	 * This MUST set the file path.
	 */
	protected abstract String getPath();
	
	/*
	 * This MUST return an instance of XMLParser.
	 */
	protected abstract XMLParser getParser();
	
	/*
	 * This inner class MUST be extended and MUST parse XML.
	 */
	protected abstract class XMLParser extends DefaultHandler {
		
	}
	
	
	
	
}
