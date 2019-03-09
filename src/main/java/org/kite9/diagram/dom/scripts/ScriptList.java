package org.kite9.diagram.dom.scripts;

import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;

import java.util.LinkedHashSet;

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ScriptList extends LinkedHashSet<String> {
	
	/**
	 * Scripts need to be embedded inside a <defs> tag in safari, afaict.
	 * @param d
	 */
	public void appendScriptTags(Document d) {
		Element svg = d.getDocumentElement();
		NodeList nl = d.getElementsByTagNameNS(SVG_NAMESPACE_URI, "defs");
		Element defs = null;
		if (nl.getLength() > 0) {
			defs = (Element) nl.item(nl.getLength()-1);
		} else {
			defs = d.createElementNS(SVGConstants.SVG_NAMESPACE_URI, "defs");
			svg.appendChild(defs);
		}
		
		Element scriptTag = d.createElementNS(SVGConstants.SVG_NAMESPACE_URI, "script");
		scriptTag.setAttribute("type", "module");
		defs.appendChild(scriptTag);
		scriptTag.setTextContent(stream().map(s -> "import \""+s+"\";\n").reduce("\n", String::concat));	
	}
}
