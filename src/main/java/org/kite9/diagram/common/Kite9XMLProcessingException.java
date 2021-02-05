package org.kite9.diagram.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

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
		this.xml = correctXml(arg1, xml);
		this.css = correctCss(arg1, css);
	}
	
	private static String correctCss(Throwable arg1, String css2) {
		if (arg1 instanceof Kite9XMLProcessingException) {
			return ((Kite9XMLProcessingException) arg1).getCss();
		} else {
			return css2;
		}
	}

	private static String correctXml(Throwable arg1, String xml2) {
		if (arg1 instanceof Kite9XMLProcessingException) {
			return ((Kite9XMLProcessingException) arg1).getContext();
		} else {
			return xml2;
		}
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
				try {
					String name = e.getPropertyName(i);
					Value v = el.getCSSStyleProperty(name);
					
					if (v != null) {
						out.add("  "+name+": "+v+";");
					}
				} catch (Exception e1) {
					// we'll absorb this exception, since it's just debug anyway.
				}
			}
		}
		
		Collections.sort(out);
		return "* {\n"+out.stream().reduce("", (a, b) -> a+"\n"+b)+"}";
	}

	public Kite9XMLProcessingException(String reason, Throwable arg1) {
		super(reason, arg1);
		this.xml = correctXml(arg1, null);
		this.css = correctCss(arg1, null);
	}

	public Kite9XMLProcessingException(String reason, Node n) {
		this(reason, null, n);
	}

	public static String toString(Node n) {
		try {
			return getPath(n)+ "\n" + new XMLHelper().toXML(n);
		} catch (Exception e) {
			return "Couldn't create XML representation: "+e.getMessage();
		}
	}
	
	public static String getPath(Node n) {
		if (n instanceof Document) {
			return "";
		} else {
			return getPath(n.getParentNode())+ " > " + n.toString();
		}
		
	}
	
	public String getContext() {
		return xml;
	}
	
	public String getCss() {
		return css;
	}
}
