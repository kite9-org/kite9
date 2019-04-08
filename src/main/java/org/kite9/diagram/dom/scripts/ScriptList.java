package org.kite9.diagram.dom.scripts;

import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.batik.util.SVGConstants;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ScriptList {

	private Set<String> uris = new LinkedHashSet<>();
	private Map<String, Object> params = new LinkedHashMap<>();

	
	public void set(String name, Object value) {
		params.put(name, value);
	}
	
	public void add(String uri) {
		uris.add(uri);
	}
		
	
	/**
	 * Scripts need to be embedded inside a <defs> tag in safari, afaict.
	 * @param d
	 */
	public void appendScriptTags(Document d) {
		if ((uris.size() >0) || (params.size() > 0)) {
			Element svg = d.getDocumentElement();
			NodeList nl = d.getElementsByTagNameNS(SVG_NAMESPACE_URI, "defs");
			Element defs = null;
			if (nl.getLength() > 0) {
				defs = (Element) nl.item(nl.getLength()-1);
			} else {
				defs = d.createElementNS(SVGConstants.SVG_NAMESPACE_URI, "defs");
				svg.appendChild(defs);
			}
			
			if (params.size() > 0) {
				Element scriptTag = createScriptTag(d, defs, false);
				scriptTag.setTextContent(getParameters());
			}
			
			if (uris.size() > 0) {
				Element scriptTag = createScriptTag(d, defs, true);
				scriptTag.setTextContent(getScriptImports());
			}			
		}
	}

	public Element createScriptTag(Document d, Element defs, boolean module) {
		Element scriptTag = d.createElementNS(SVGConstants.SVG_NAMESPACE_URI, "script");
		if (module) {
			scriptTag.setAttribute("type", "module");
		}
		defs.appendChild(scriptTag);
		return scriptTag;
	}
	
	protected String getParameters() {
		
		return "document.params = { "+ params.entrySet().stream()
				.map(e -> "'"+escape(e.getKey())+"' : "+format(e.getValue()))
				.reduce((a, b) -> a+", \n "+b)
				.orElse("")+ "\n } \n";
	}
	
	private String format(Object value) {
		if (value instanceof String) {
			return "'" + escape((String) value) +"'";
		} else if (value instanceof List<?>) {
			return "[" + ((List<?>) value).stream()
				.map(e -> format(e))
				.reduce((a, b) -> a+", "+b).orElse("")+"] ";
		} else {
			throw new Kite9ProcessingException("Unsupported content in params: "+value);
		}
	}
		
	private String escape(String value) {
		return value.replace("'", "\\'");
	}

	protected String getScriptImports() {
		return uris.stream().map(s -> "import \""+s+"\";\n").reduce("\n", String::concat);
	}
}
