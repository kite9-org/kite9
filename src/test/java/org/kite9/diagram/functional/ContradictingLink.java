package org.kite9.diagram.functional;

import org.kite9.diagram.adl.Link;
import org.kite9.diagram.adl.XMLElement;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.primitives.Label;

/** 
 * Link which allows contradiction 
 */
public class ContradictingLink extends Link {

	private static final long serialVersionUID = 1L;

	public ContradictingLink() {
		super();
	}

	public ContradictingLink(XMLElement from, XMLElement to, String fromStyle, Label fromLabel, String toEndStyle,
			Label toLabel, Direction drawDirection) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel, drawDirection);
	}

	public ContradictingLink(XMLElement from, XMLElement to, String fromStyle, Label fromLabel, String toEndStyle,
			Label toLabel) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel);
	}

	public ContradictingLink(XMLElement from, XMLElement to) {
		super(from, to);
	}

	
}
