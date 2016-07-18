package org.kite9.diagram.visualization.display.style;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.adl.ADLDocument;

public class SVGAttributedStyle {
		
	CSSStylableElement styleElement;	

	public SVGAttributedStyle(CSSStylableElement stylableElement) {
		super();
		this.styleElement = stylableElement;
	}

	protected CSSStylableElement getStyleElement() {
		return styleElement;
	}
	
	protected Value getCSSStyleProperty(String prop) {
		CSSEngine e = ((ADLDocument) getStyleElement().getOwnerDocument()).getCSSEngine();
		return e.getComputedStyle(getStyleElement(), null, e.getPropertyIndex(prop));
	}
	
	protected BridgeContext getBridgeContext() {
		CSSEngine e = ((ADLDocument) getStyleElement().getOwnerDocument()).getCSSEngine();
		return (BridgeContext) e.getCSSContext();
	}

	
}

