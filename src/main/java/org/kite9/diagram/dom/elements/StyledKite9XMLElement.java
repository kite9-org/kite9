package org.kite9.diagram.dom.elements;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.model.style.DiagramElementType;
import org.w3c.dom.svg.SVGTransformable;

public interface StyledKite9XMLElement extends CSSStylableElement, Kite9XMLElement, SVGTransformable {

	public Value getCSSStyleProperty(String prop);
	
	public DiagramElementType getType();
	
}

