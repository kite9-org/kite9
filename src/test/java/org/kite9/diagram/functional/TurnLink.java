package org.kite9.diagram.functional;

import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.XMLElement;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.primitives.Label;

/** 
 * Link which allows turns 
 */
public class TurnLink extends Link {

	private static final long serialVersionUID = 1L;

	public TurnLink() {
		super();
	}

	public TurnLink(XMLElement from, XMLElement to, String fromStyle, Label fromLabel, String toEndStyle,
			Label toLabel, Direction drawDirection) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel, drawDirection);
	}

	public TurnLink(XMLElement from, XMLElement to, String fromStyle, Label fromLabel, String toEndStyle,
			Label toLabel) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel, null);
	}

	public TurnLink(XMLElement from, XMLElement to) {
		super(from, to);
	}

	
}
