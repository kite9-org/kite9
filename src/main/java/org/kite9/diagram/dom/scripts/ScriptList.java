package org.kite9.diagram.dom.scripts;

import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.batik.css.engine.ImportRule;
import org.apache.batik.css.engine.Rule;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.css.AtParamsRule;
import org.kite9.diagram.dom.css.AtScriptRule;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ScriptList {

	private Set<String> uris = new LinkedHashSet<>();
	private Map<String, Object> params = new LinkedHashMap<>();
	
	public ScriptList(List<StyleSheet> list) {
		this();
   		list.forEach(ss -> loadScripts(ss));
	}

	@SuppressWarnings("unchecked")
	private void loadScripts(StyleSheet sheet) {
		for (int j = 0; j < sheet.getSize(); j++) {
			Rule r = sheet.getRule(j); 
			if (r instanceof AtParamsRule) {
				AtParamsRule atParamsRule = (AtParamsRule) r;
				Object value = atParamsRule.getValue();
				String name = atParamsRule.getName();
				if (value instanceof String) {
					set(name, value);
				} else if (value instanceof List) {
					add(name, (List<String>) value); 
				}
			} else if (r instanceof AtScriptRule) {
				AtScriptRule atScriptRule = (AtScriptRule) r;
				uris.add(atScriptRule.getUri());
			} else if (r instanceof ImportRule) {
				loadScripts((ImportRule) r); 
			}
		}
	}

	public ScriptList() {
	}
	
	public void set(String name, Object value) {
		params.put(name, value);
	}
	
	public void add(String uri) {
		uris.add(uri);
	}
	
	@SuppressWarnings("unchecked")
	public void add(String name, List<String> additionalValues) {
		Object p = params.get(name);
		if (p == null) {
			params.put(name, additionalValues);
		} else if (p instanceof List) {
			((List<String>) p).addAll(additionalValues);
		} else {
			throw new Kite9ProcessingException("Can't add to param "+name+" as it is a single value "+p);
		}
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
			throw new LogicException("Unsupported content in params: "+value);
		}
	}
		
	private String escape(String value) {
		return value.replace("'", "\\'");
	}

	protected String getScriptImports() {
		return uris.stream().map(s -> "import \""+s+"\";\n").reduce("\n", String::concat);
	}

	
}
