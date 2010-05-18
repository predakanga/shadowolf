package com.shadowolf.core.config;

import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.stream.XMLStreamException;

/**
 * You should not mess with this class.  It's public for XML parsing visibility reasons.
 * <br />
 * Edit at your own risk.  As such, it is undocumented.
 */
public class Parameter implements XMLSerializable {
	private static final long serialVersionUID = -169768234152432094L;
	private String text;
	private String name;
	
	public String getText() {
		return this.text;
	}
	
	public void setText(String text) {
		this.text = text.trim();
	}
	
	public void setName(String name) {
		this.name = name.trim();
	}

	public String getName() {
		return name;
	}

	protected static XMLFormat<Parameter> TEXTELE_XML = new XMLFormat<Parameter>(Parameter.class) {

		@Override
		public void read(XMLFormat.InputElement xml, Parameter obj) throws XMLStreamException {
			obj.setName(xml.getAttribute("name").toString());
			obj.setText(xml.getText().toString());
		}

		@Override
		public void write(Parameter obj, XMLFormat.OutputElement xml) throws XMLStreamException {
			// TODO Auto-generated method stub
			
		}
		
	};
}
