package org.kite9.diagram.functional;

import org.kite9.diagram.adl.Link;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.xml.XMLElement;

/** 
 * Link which allows turns 
 */
public class TurnLink extends Link {

	private static final long serialVersionUID = 1L;
	public static final String TURN = "turn";

	
	public TurnLink() {
		super();
	}

	public TurnLink(XMLElement from, XMLElement to, String fromStyle, XMLElement fromLabel, String toEndStyle,
			XMLElement toLabel, Direction drawDirection) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel, drawDirection);
		setAttribute(LINK_TEST, TURN);
	}

	public TurnLink(XMLElement from, XMLElement to, String fromStyle, XMLElement fromLabel, String toEndStyle,
			XMLElement toLabel) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel, null);
		setAttribute(LINK_TEST, TURN);
	}

	public TurnLink(XMLElement from, XMLElement to) {
		super(from, to);
		setAttribute(LINK_TEST, TURN);
	}

	
}
