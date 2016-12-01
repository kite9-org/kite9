package org.kite9.diagram.visualization.display.style;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.style.impl.AbstractXMLDiagramElement;
import org.kite9.diagram.xml.StyledXMLElement;

public class SVGAttributedStyle {
		
	DiagramElement styleElement;	

	public SVGAttributedStyle(DiagramElement stylableElement) {
		super();
		this.styleElement = stylableElement;
	}
	
	protected Value getCSSStyleProperty(String prop) {
		return styleElement.getCSSStyleProperty(prop);
	}
	
	protected BridgeContext getBridgeContext() {
		CSSEngine e = getStyleElement().getOwnerDocument().getCSSEngine();
		return (BridgeContext) e.getCSSContext();
	}

	protected StyledXMLElement getStyleElement() {
		return ((AbstractXMLDiagramElement)styleElement).getTheElement();
	}

	
}

