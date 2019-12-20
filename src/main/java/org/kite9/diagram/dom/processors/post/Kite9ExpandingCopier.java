package org.kite9.diagram.dom.processors.post;

import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.processors.copier.PrefixingCopier;
import org.kite9.diagram.dom.processors.copier.ValueReplacingCopier;
import org.w3c.dom.Node;

/**
 * Handles Kite9 elements, where they expand and produce their own SVG output.
 * 
 * @author robmoffat
 *
 */
public class Kite9ExpandingCopier extends PrefixingCopier {

	public Kite9ExpandingCopier(String newPrefix, Node destination) {
		super(destination, new DocumentValueReplacer(destination.getOwnerDocument()), "");
	}
	

	@Override
	public void processContents(Node n, Node inside) {
		if (n instanceof Kite9XMLElement) {
			Node copy = ((Kite9XMLElement) n).output(inside.getOwnerDocument(), this);
			if (copy != null) {
				inside.appendChild(copy);
			}
		} else {
			super.processContents(n, inside);
		}
	}	
}
