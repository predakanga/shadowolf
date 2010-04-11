package com.shadowolf.config;

import java.util.HashMap;
import java.util.Stack;

public class Element {
	private String text;
	private String name;
	private final Stack<Element> children = new Stack<Element>();
	private final HashMap<String, String> attributes = new HashMap<String, String>();

	public String getAttribute(final String name) {
		return this.attributes.get(name);
	}

	public String getName() {
		return this.name;
	}

	public Stack<Element> getChildren() {
		return this.children;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void addChild(final Element e) {
		this.children.add(e);
	}

	public void setAttribute(final String name, final String value) {
		this.attributes.put(name, value);
	}

	public String getText() {
		return this.text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	public boolean hasChildren() {
		return !this.children.empty();
	}

	public int numChildren() {
		return this.children.size();
	}
}
