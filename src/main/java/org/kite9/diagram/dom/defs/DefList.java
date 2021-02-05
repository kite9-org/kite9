package org.kite9.diagram.dom.defs;

import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.batik.css.engine.ImportRule;
import org.apache.batik.css.engine.Rule;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.batik.util.SVG12Constants;
import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.dom.css.AtDefsRule;
import org.kite9.diagram.dom.css.AtParamsRule;
import org.kite9.diagram.dom.css.AtScriptRule;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.processors.copier.PrefixingCopier;
import org.kite9.diagram.dom.processors.xpath.NullValueReplacer;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.logging.LogicException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGSVGElement;

public class DefList {

	private Set<String> defsUris = new LinkedHashSet<String>();
	private Set<String> scriptUris = new LinkedHashSet<>();
	private Map<String, Object> params = new LinkedHashMap<>();
	
	public DefList(List<StyleSheet> list) {
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
				scriptUris.add(atScriptRule.getUri());
			} else if (r instanceof AtDefsRule) {
				AtDefsRule atDefsRule = (AtDefsRule) r;
				defsUris.add(atDefsRule.getUri());
			} else if (r instanceof ImportRule) {
				loadScripts((ImportRule) r); 
			}
		}
	}

	public DefList() {
	}
	
	public void set(String name, Object value) {
		params.put(name, value);
	}
	
	public void add(String uri) {
		scriptUris.add(uri);
	}
	
	public void add(String name, List<String> additionalValues) {
		params.put(name, additionalValues);
	}		
	
	/**
	 * Scripts need to be embedded inside a <defs> tag in safari, afaict.
	 */
	public void appendDefsAndScripts(Document d, Kite9DocumentLoader docLoader) {
		for (String uri : defsUris) {
			importDefs(d, uri, docLoader);
		}
		
		if ((scriptUris.size() >0) || (params.size() > 0)) {
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
			
			if (scriptUris.size() > 0) {
				Element scriptTag = createScriptTag(d, defs, true);
				scriptTag.setTextContent(getScriptImports());
			}			
		}
	}
	
	public void importDefs(Document toDocument, String uriFrom, Kite9DocumentLoader docLoader) {
		try {
			SVGSVGElement top = (SVGSVGElement) toDocument.getDocumentElement();
			String prefix = top.getPrefix();
			String namespace = top.getNamespaceURI();
	
			Document source = docLoader.loadDocument(uriFrom);
			NodeList defs = source.getElementsByTagNameNS(SVG12Constants.SVG_NAMESPACE_URI, SVG12Constants.SVG_DEFS_TAG);
			
			for (int i = 0; i < defs.getLength(); i++) {
				Element def = (Element) defs.item(i);
				Element newDef = toDocument.createElementNS(SVG12Constants.SVG_NAMESPACE_URI, SVG12Constants.SVG_DEFS_TAG);
				newDef.setPrefix(prefix);
				top.insertBefore(newDef, null);
				XMLProcessor c = new PrefixingCopier(newDef, false, new NullValueReplacer(), prefix, namespace);
				c.processContents(def);
			}
		} catch (IOException e) {
			throw new Kite9XMLProcessingException("Error collecting defs from uri:  "+uriFrom, e, toDocument);
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
		return scriptUris.stream().map(s -> "import \""+s+"\";\n").reduce("\n", String::concat);
	}

	
}
