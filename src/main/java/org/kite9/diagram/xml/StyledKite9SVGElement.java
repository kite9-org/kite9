package org.kite9.diagram.xml;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.w3c.dom.svg.SVGTransformable;

public interface StyledKite9SVGElement extends CSSStylableElement, XMLElement, SVGTransformable {

	public void setClasses(String c);
	
	public String getClasses();
	
	public void setStyle(String s);
	
	public void setShapeName(String s);
	
	public String getShapeName();
	
	public Value getCSSStyleProperty(String prop);
}


