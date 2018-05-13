package org.kite9.diagram.dom.processors.copier;

import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.w3c.dom.Node;

/**
 * Handles Kite9 elements, where they expand and produce their own SVG output.
 * 
 * @author robmoffat
 *
 */
public class Kite9ExpandingCopier extends PrefixingCopier {

	public Kite9ExpandingCopier(String newPrefix, Node destination) {
		super(newPrefix, destination);
	}

	@Override
	protected Node copyChild(Node n, Node to) {
		Node copy = null;
		if (n instanceof Kite9XMLElement) {
			copy = ((Kite9XMLElement) n).output(to.getOwnerDocument());
			if (copy != null) {
				to.appendChild(copy);
			}
		} else {
			copy = super.copyChild(n, to);
		}
		
		return copy;
	}
	
}
