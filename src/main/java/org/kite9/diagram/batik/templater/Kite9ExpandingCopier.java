package org.kite9.diagram.batik.templater;

import org.kite9.framework.xml.Kite9XMLElement;
import org.w3c.dom.Node;

/**
 * Handles Kite9 elements, where they expand and produce their own SVG output.
 * 
 * @author robmoffat
 *
 */
public class Kite9ExpandingCopier extends PrefixingCopier {

	public Kite9ExpandingCopier(String newPrefix) {
		super(newPrefix);
	}

	@Override
	protected Node processNode(Node n, Node to) {
		Node copy = null;
		if (n instanceof Kite9XMLElement) {
			copy = ((Kite9XMLElement) n).output(to.getOwnerDocument(), this);
			to.appendChild(copy);	
		} else {
			copy = super.processNode(n, to);
		}
		
		return copy;
	}
	
}
