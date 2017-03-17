package org.kite9.diagram.adl;

import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.xml.XMLElement;

/** 
 * Link which allows contradiction 
 */
public class ContradictingLink extends Link {

	private static final long serialVersionUID = 1L;
	
	public static final String CONTRADICTING = "contradicting";

	public ContradictingLink() {
		super();
	}

	public ContradictingLink(XMLElement from, XMLElement to, String fromStyle, XMLElement fromLabel, String toEndStyle,
			XMLElement toLabel, Direction drawDirection) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel, drawDirection);
		setAttribute(LINK_TEST, CONTRADICTING);
	}

	public ContradictingLink(XMLElement from, XMLElement to, String fromStyle, XMLElement fromLabel, String toEndStyle,
			XMLElement toLabel) {
		super(from, to, fromStyle, fromLabel, toEndStyle, toLabel);
		setAttribute(LINK_TEST, CONTRADICTING);
	}

	public ContradictingLink(XMLElement from, XMLElement to) {
		super(from, to);
		setAttribute(LINK_TEST, CONTRADICTING);
	}

	
}
