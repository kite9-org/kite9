package org.kite9.diagram.visualization.batik;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeExtension;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.SVGBridgeExtension;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.bridge.svg12.SVG12BridgeExtension;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.SystemColorSupport;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.script.InterpreterPool;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.framework.serialization.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public final class ADLBridgeContext extends SVG12BridgeContext  {
//	private final ADLDocument ownerDocument;


	public ADLBridgeContext(UserAgent userAgent, DocumentLoader loader) {
		super(userAgent, loader);
	}

	public ADLBridgeContext(UserAgent userAgent, InterpreterPool interpreterPool, DocumentLoader documentLoader) {
		super(userAgent, interpreterPool, documentLoader);
	}

	public ADLBridgeContext(UserAgent userAgent) {
		super(userAgent);
	}

	public boolean isInteractive() {
		return false;
	}

	public boolean isDynamic() {
		return false;
	}

	public Value getSystemColor(String ident) {
	    return SystemColorSupport.getSystemColor(ident);
	}

	public float getPixelUnitToMillimeter() {
	    return 0.26458333333333333333333333333333f; // 96dpi
	}

	public float getPixelToMillimeter() {
		return getPixelUnitToMillimeter();
	}

	public float getMediumFontSize() {
	    return 9f * 25.4f / (72f * getPixelUnitToMillimeter());
	}

	public float getLighterFontWeight(float f) {
	    // Round f to nearest 100...
	    int weight = ((int)((f+50)/100))*100;
	    switch (weight) {
	    case 100: return 100;
	    case 200: return 100;
	    case 300: return 200;
	    case 400: return 300;
	    case 500: return 400;
	    case 600: return 400;
	    case 700: return 400;
	    case 800: return 400;
	    case 900: return 400;
	    default:
	        throw new IllegalArgumentException("Bad Font Weight: " + f);
	    }
	}

//	public Value getDefaultFontFamily() {
//	    // No cache needed since the default font family is asked only
//	    // one time on the root element (only if it does not have its
//	    // own font-family).
//		StyledXMLElement root = (StyledXMLElement)ownerDocument.getFirstChild();
//	    String str = "Arial, Helvetica, sans-serif";
//	    return ownerDocument.getCSSEngine().parsePropertyValue
//	        (root,SVGConstants.CSS_FONT_FAMILY_PROPERTY, str);
//	}

	public CSSEngine getCSSEngineForElement(Element e) {
		ADLDocument doc = (ADLDocument)e.getOwnerDocument();
	    return doc.getCSSEngine();
	}

	public float getBolderFontWeight(float f) {
	    // Round f to nearest 100...
	    int weight = ((int)((f+50)/100))*100;
	    switch (weight) {
	    case 100: return 600;
	    case 200: return 600;
	    case 300: return 600;
	    case 400: return 600;
	    case 500: return 600;
	    case 600: return 700;
	    case 700: return 800;
	    case 800: return 900;
	    case 900: return 900;
	    default:
	        throw new IllegalArgumentException("Bad Font Weight: " + f);
	    }
	}

	public float getBlockWidth(Element elt) {
	    throw new UnsupportedOperationException("We don't support viewports");
	}

	public float getBlockHeight(Element elt) {
	    throw new UnsupportedOperationException("We don't support viewports");
	}

	public void checkLoadExternalResource(ParsedURL externalResourceURL, ParsedURL docURL) throws SecurityException {
		 
	}
	
	/**
	 * This needs to be here to prevent the parent inspecting the root node and
	 * discovering it's not <svg>.
	 */
	@SuppressWarnings(value = { "unchecked", "rawtypes" })
	public List getBridgeExtensions(Document doc) {
        BridgeExtension svgBE = new SVG12BridgeExtension();

        float priority = svgBE.getPriority();
        extensions = new LinkedList(getGlobalBridgeExtensions());

        ListIterator li = extensions.listIterator();
        for (;;) {
            if (!li.hasNext()) {
                li.add(svgBE);
                break;
            }
            BridgeExtension lbe = (BridgeExtension)li.next();
            if (lbe.getPriority() > priority) {
                li.previous();
                li.add(svgBE);
                break;
            }
        }

        return extensions;
    }
	
	@Override
	public Bridge getBridge(String namespaceURI, String localName) {
		if (XMLHelper.KITE9_NAMESPACE.equals(namespaceURI)) {
			return new Kite9Bridge();
		} else {
			return super.getBridge(namespaceURI, localName);
		}
	}
	
	
}