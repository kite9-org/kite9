package org.kite9.diagram.adl;

import org.kite9.diagram.model.position.Direction;
import org.w3c.dom.Element;

/** 
 * Link which allows turns 
 */
public class TurnLink extends Link {

	private static final long serialVersionUID = 1L;
	public static final String TURN = "turn";

	
	public TurnLink() {
		super();
	}

	public TurnLink(Element from, Element to, String fromStyle, Element fromLabel, String toEndStyle,
					Element toLabel, Direction drawDirection) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel, drawDirection);
		setAttribute(LINK_TEST, TURN);
	}

	public TurnLink(Element from, Element to, String fromStyle, Element fromLabel, String toEndStyle,
			Element toLabel) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel, null);
		setAttribute(LINK_TEST, TURN);
	}

	public TurnLink(Element from, Element to) {
		super(from, to);
		setAttribute(LINK_TEST, TURN);
	}

	
}
