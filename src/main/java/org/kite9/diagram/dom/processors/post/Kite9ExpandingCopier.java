package org.kite9.diagram.dom.processors.post;

import javax.print.attribute.standard.Destination;

import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.processors.copier.ValueReplacingCopier;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles Kite9 elements, where they expand and produce their own SVG output.
 * 
 * @author robmoffat
 *
 */
public class Kite9ExpandingCopier extends ValueReplacingCopier {

	public Kite9ExpandingCopier(String newPrefix, Node destination) {
		super(destination, newPrefix, new DocumentValueReplacer(destination.getOwnerDocument()));
	}
	
	@Override
	public Node processContents(Node from) {
		return processContents(from, null);
	}

//	@Override
//	public Node processContents(Node from, Node inside) {
//		if (from instanceof Kite9XMLElement) {
//			processContents(from.getChildNodes(), inside);
//			return from;
//		} else {
//			return super.processContents(from, inside);
//		}
//	}
//	
	

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
