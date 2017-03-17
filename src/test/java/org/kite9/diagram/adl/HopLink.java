package org.kite9.diagram.adl;

import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.xml.XMLElement;

/** 
 * Link which allows hops 
 */
public class HopLink extends Link {

	private static final long serialVersionUID = 1L;
	public static final String HOP = "hop";

	public HopLink() {
		super();
	}

	public HopLink(XMLElement from, XMLElement to, String fromStyle, XMLElement fromLabel, String toEndStyle,
			XMLElement toLabel, Direction drawDirection) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel, drawDirection);
		setAttribute(LINK_TEST, HOP);

	}

	public HopLink(XMLElement from, XMLElement to, String fromStyle, XMLElement fromLabel, String toEndStyle,
			XMLElement toLabel) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel);
		setAttribute(LINK_TEST, HOP);
	}

	public HopLink(XMLElement from, XMLElement to) {
		super(from, to);
		setAttribute(LINK_TEST, HOP);
	}

	
}
