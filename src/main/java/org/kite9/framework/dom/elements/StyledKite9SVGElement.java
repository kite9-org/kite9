package org.kite9.framework.dom.elements;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.svg.SVGTransformable;

public interface StyledKite9SVGElement extends CSSStylableElement, Kite9XMLElement, SVGTransformable {

	public Value getCSSStyleProperty(String prop);
}


