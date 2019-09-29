package org.kite9.framework.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.w3c.dom.Node;

/**
 * Allows the context to be passed as a specific piece of XML.
 * @author robmoffat
 *
 */
public class Kite9XMLProcessingException extends Kite9ProcessingException {

	private final String xml;
	private final String css;

	public Kite9XMLProcessingException(String reason, Throwable arg1, String xml, String css) {
		super(reason, arg1);
		this.xml = xml;
		this.css = css;
	}
	
	public Kite9XMLProcessingException(String reason, Throwable arg1, Node n) {
		this(reason, arg1, toString(n), debugCss(n));
	}
	
	public static String debugCss(Node n) {
		List<String> out = new ArrayList<>();
		if (n instanceof StyledKite9XMLElement) {
			StyledKite9XMLElement el = (StyledKite9XMLElement) n;
			ADLDocument doc = el.getOwnerDocument();
			CSSEngine e = doc.getCSSEngine();
			for (int i = 0; i < e.getNumberOfProperties(); i++) {
				String name = e.getPropertyName(i);
				Value v = el.getCSSStyleProperty(name);
				
				if (v != null) {
					out.add("  "+name+": "+v+";");
				}
			}
		}
		
		Collections.sort(out);
		return "* {\n"+out.stream().reduce("", (a, b) -> a+"\n"+b)+"}";
	}

	public Kite9XMLProcessingException(String reason, Throwable arg1) {
		super(reason, arg1);
		this.xml = null;
		this.css = null;
	}

	public Kite9XMLProcessingException(String reason, Node n) {
		this(reason, null, n);
	}

	public static String toString(Node n) {
		try {
			return new XMLHelper().toXML(n);
		} catch (Exception e) {
			return "Couldn't create XML representation: "+e.getMessage();
		}
	}
	
	public String getContext() {
		return xml;
	}
	
	public String getCss() {
		return css;
	}
}
