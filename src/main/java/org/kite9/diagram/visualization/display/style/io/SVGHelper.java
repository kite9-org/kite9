package org.kite9.diagram.visualization.display.style.io;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.anim.dom.SVGOMRectElement;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGConstants;
import org.kite9.framework.logging.LogicException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SVGHelper {
	
	private DocumentBuilder builder;
	private SVGDOMImplementation domImpl = (SVGDOMImplementation) SVGDOMImplementation.getDOMImplementation();
	private SVG12BridgeContext bridge = new SVG12BridgeContext(new UserAgentAdapter());
	private CSSEngine engine;
	private SVGOMDocument doc;
	
	public SVGOMDocument getDoc() {
		return doc;
	}

	private SVGGeneratorContext context;
	private Element root;
	
	public SVG12BridgeContext getBridge() {
		return bridge;
	}

	private GradientPaintValueManager gpvm;
	private DasharrayValueManager davm;


	public SVGHelper() {
		super();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			gpvm = new GradientPaintValueManager(this);
			davm = new DasharrayValueManager();
			doc = newSVGDocument();
			context = SVGGeneratorContext.createDefault(doc);
			engine = doc.getCSSEngine();
			root = addRootElement(doc);
			
		} catch (ParserConfigurationException e1) {
			throw new LogicException("Parser problem: ", e1);
		}
	}

	private SVGOMDocument newSVGDocument() {
		SVGOMDocument doc = (SVGOMDocument) domImpl.createDocument(SVGConstants.SVG_NAMESPACE_URI, null, null);
		CSSEngine eng = createCSSEngine(doc);
		int idx = eng.getPropertyIndex(CSSConstants.CSS_FILL_PROPERTY);
		eng.getValueManagers()[idx] = gpvm;
		idx = eng.getPropertyIndex(CSSConstants.CSS_STROKE_DASHARRAY_PROPERTY);
		eng.getValueManagers()[idx] = davm;
		doc.setCSSEngine(eng);
		return doc;
	}

	private  CSSEngine createCSSEngine(SVGOMDocument doc) {
		return domImpl.createCSSEngine(doc, bridge);
	}
	
	private Element addRootElement(SVGOMDocument doc) {
		Element root = doc.createElement("svg");
		doc.appendChild(root);
		return root;
	}

	public CSSStylableElement createStyleableElement() {
		 return new SVGOMRectElement(null, doc);
	}
	
	public static Value getCSSStyleProperty(CSSStylableElement e, String prop) {
		SVGOMDocument doc = (SVGOMDocument) e.getOwnerDocument();
		CSSEngine engine = doc.getCSSEngine();
		int idx = engine.getPropertyIndex(prop);
		Value v = CSSUtilities.getComputedStyle(e, idx);
		return v;
	}

	public GradientPaintValueManager getGradientPaintValueManager() {
		return gpvm;
	}

	public Node getRoot() {
		return root;
	}

	public SVGGeneratorContext getContext() {
		return context;
	}
}
