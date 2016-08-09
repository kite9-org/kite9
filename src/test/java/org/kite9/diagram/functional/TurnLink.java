package org.kite9.diagram.functional;

import org.kite9.diagram.adl.Label;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.xml.Link;
import org.kite9.diagram.xml.XMLElement;

/** 
 * Link which allows turns 
 */
public class TurnLink extends Link {

	private static final long serialVersionUID = 1L;

	public TurnLink() {
		super();
	}

	public TurnLink(XMLElement from, XMLElement to, String fromStyle, XMLElement fromLabel, String toEndStyle,
			XMLElement toLabel, Direction drawDirection) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel, drawDirection);
	}

	public TurnLink(XMLElement from, XMLElement to, String fromStyle, XMLElement fromLabel, String toEndStyle,
			XMLElement toLabel) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel, null);
	}

	public TurnLink(XMLElement from, XMLElement to) {
		super(from, to);
	}

	
}
