package org.kite9.diagram.common;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Allows the context to be passed as a specific piece of XML.
 * @author robmoffat
 *
 */
public class Kite9XMLProcessingException extends Kite9ProcessingException {

	private final String context;
	private final String css;
	private final String complete;

	public Kite9XMLProcessingException(String reason, Throwable arg1, String context, String css, String complete) {
		super(reason, arg1);
		this.context = correctContext(arg1, context);
		this.css = correctCss(arg1, css);
		this.complete  = correctComplete(arg1, complete);
	}
	
	private static String correctCss(Throwable arg1, String css2) {
		if (arg1 instanceof Kite9XMLProcessingException) {
			return ((Kite9XMLProcessingException) arg1).getCss();
		} else {
			return css2;
		}
	}

	private static String correctContext(Throwable arg1, String xml2) {
		if (arg1 instanceof Kite9XMLProcessingException) {
			return ((Kite9XMLProcessingException) arg1).getContext();
		} else {
			return xml2;
		}
	}

	private static String correctComplete(Throwable arg1, String xml2) {
		if (arg1 instanceof Kite9XMLProcessingException) {
			return ((Kite9XMLProcessingException) arg1).getComplete();
		} else {
			return xml2;
		}
	}

	public Kite9XMLProcessingException(String reason, Throwable arg1, Node n, Document doc) {
		this(reason, arg1, toString(n), debugCss(n), toString(doc));
	}

	public Kite9XMLProcessingException(String reason, Throwable arg1, Node n) {
		this(reason, arg1, n, n.getOwnerDocument());
	}

	public static String debugCss(Node n) {
		List<String> out = new ArrayList<>();
		if (n instanceof CSSStylableElement) {
			CSSStylableElement el = (CSSStylableElement) n;
			SVGOMDocument doc = (SVGOMDocument) n.getOwnerDocument();
			CSSEngine e = doc.getCSSEngine();
			for (int i = 0; i < e.getNumberOfProperties(); i++) {
				try {
					String name = e.getPropertyName(i);
					Value v = e.getComputedStyle(el, null, i);
					
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
		this.context = correctContext(arg1, null);
		this.css = correctCss(arg1, null);
		this.complete = correctComplete(arg1, null);
	}

	public Kite9XMLProcessingException(String reason, Node n) {
		this(reason, null, n, n.getOwnerDocument());
	}

	public static String toString(Node n) {
		if (n==null) {
			return "";
		}
		try {
			return getPath(n)+ "\n" + new XMLHelper().toXML(n);
		} catch (Exception e) {
			return "Couldn't create XML representation: "+e.getMessage();
		}
	}
	
	public static String getPath(Node n) {
		if (n instanceof Document) {
			return "";
		} else if (n instanceof Element) {
			String tag = ((Element) n).getTagName();
			String id = ((Element) n).getAttribute("id");
			StringBuilder path = new StringBuilder();
			path.append("<");
			path.append(tag);
			if ((id != null) && (id.length() > 0)) {
				path.append(" id=\"");
				path.append(id);
				path.append("\"");
			}
			path.append(">");

			return getPath(n.getParentNode()) + " > " + path.toString();
		} else if (n == null) {
			return "";
 		} else {
			return getPath(n.getParentNode())+ " > " + new XMLHelper().toXML(n);
		}
	}
	
	public String getContext() {
		return context;
	}

	public String getComplete() {
		return complete;
	}

	public String getCss() {
		return css;
	}
}
