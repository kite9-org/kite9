package org.kite9.diagram.dom.processors.post;

import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.processors.copier.PrefixingCopier;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles Kite9 elements, where they expand and produce their own SVG output.
 * 
 * @author robmoffat
 *
 */
public class Kite9ExpandingCopier extends PrefixingCopier {

	public Kite9ExpandingCopier(String newPrefix, Node destination) {
		super(destination, new DocumentValueReplacer(getDocument(destination)),
			"", SVGConstants.SVG_NAMESPACE_URI);
	}
	
	@Override
	protected Element processTag(Element from) {
		if (from instanceof Kite9XMLElement) {
			Element out = getDestinationDocument().createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
			return out;
		} else {
			return super.processTag(from);
		}
	}

	protected void processContents(NodeList contents, Node inside) {
		for (int i = 0; i < contents.getLength(); i++) {
			Node n = contents.item(i);
			Node out = null;
			if (n instanceof Kite9XMLElement) {
				out = ((Kite9XMLElement) n).output(inside.getOwnerDocument(), this);
			} else {
				out = processContents(n, inside);
			}
			if (out != null) {
				inside.appendChild(out);
			}
		}
	}


	@Override
	protected boolean canValueReplace(Node n) {
		return true;
	}	
}
