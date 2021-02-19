package org.kite9.diagram.dom.processors.post;

import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.processors.copier.PrefixingCopier;
import org.kite9.diagram.dom.processors.xpath.ValueReplacer;
import org.w3c.dom.Document;
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

	public Kite9ExpandingCopier(String newPrefix, Document destination, ValueReplacer vr) {
		super(destination, true, vr, "", SVGConstants.SVG_NAMESPACE_URI);
	}
	
	@Override
	protected Element processTag(Element from) {
		if (from instanceof Kite9XMLElement) {
			Element out = getDestinationDocument().createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
			if (!out.hasAttribute("k9-elem")) {
				out.setAttribute("k9-elem", from.getTagName());
			}
			return out;
		} else {
			return super.processTag(from);
		}
	}

	protected void processNodeList(NodeList contents, Node inside) {
		for (int i = 0; i < contents.getLength(); i++) {
			Node n = contents.item(i);
			Node out = null;
			if (n instanceof Kite9XMLElement) {
				out = ((Kite9XMLElement) n).output((Document) destination, this);
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

	/**
	 * Since we are going to call this at various points in the 
	 * xml document, we can't rely on the destination being correct.
	 */
	@Override
	public Node processContents(Node n) {
		return processContents(n, null);
	}	
	
	
	
}
