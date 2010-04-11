package com.shadowolf.config;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class Parser extends DefaultHandler {
	private final Stack<Element> workingSet = new Stack<Element>();
	private Element rootElement;

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		super.characters(ch, start, length);
		final Element current = this.workingSet.peek();

		current.setText(new String(ch, start, length));
	}

	public Element getRootElement() {
		return this.rootElement;
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		super.endElement(uri, localName, qName);

		final Element popped = this.workingSet.pop();
		if(this.workingSet.size() > 0) {
			this.workingSet.peek().addChild(popped);
		} else {
			this.rootElement = popped;
		}
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		final Element next = new Element();
		next.setName(qName);

		for(int i = 0; i < attributes.getLength(); i++) {
			if(!attributes.getLocalName(i).equals("xmlns")) {
				next.setAttribute(attributes.getLocalName(i), attributes.getValue(i));
			}

		}

		this.workingSet.push(next);
	}

	public Parser(final String filePath) throws ParserConfigurationException, SAXException, IOException {
		final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(new File(filePath), this);

	}
}