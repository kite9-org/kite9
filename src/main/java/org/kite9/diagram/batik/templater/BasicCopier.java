package org.kite9.diagram.batik.templater;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.kite9.framework.xml.ADLDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Contains basic functionality for copying the contents of one XML node 
 * into another.
 * 
 * @author robmoffat
 *
 */
public class BasicCopier extends AbstractXMLProcessor {

	protected Node processNode(Node n, Node inside) {
		Node copy = n.cloneNode(false);
		
		Document ownerDocument = inside.getOwnerDocument();
		ownerDocument.adoptNode(copy);
		
		if (copy instanceof Element) {
			checkIDElement((Element) copy);
		}
		
		inside.appendChild(copy);
		return copy;
	}

	/**
	 * Forces re-indexing the IDs within the destination document
	 */
	private void checkIDElement(Element copy) {
		if (copy.hasAttribute("id")) {
			String id = copy.getAttribute("id");
			((SVGOMDocument) copy.getOwnerDocument()).updateIdEntry(copy, null, id);
		}
	}
}
