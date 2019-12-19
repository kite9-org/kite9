package org.kite9.diagram.dom.elements;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.css.ElementCSSInlineStyle;
import org.w3c.dom.svg.SVGTransformable;

public interface StyledKite9XMLElement extends CSSStylableElement, Kite9XMLElement, SVGTransformable, ElementCSSInlineStyle {

	public Value getCSSStyleProperty(String prop);


}


