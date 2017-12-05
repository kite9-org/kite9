package org.kite9.diagram.batik.templater;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Contains basic functionality for copying the contents of one XML node 
 * into another.
 * 
 * @author robmoffat
 *
 */
public class BasicCopier implements XMLProcessor {
	
	private Node destination;
	
	public BasicCopier(Node destination) {
		this.destination = destination;
	}
	
	protected void copyContents(Node from, Node to) {
		NodeList nl = from.getChildNodes();
		copyContents(nl, to);
	}

	protected void copyContents(NodeList nl, Node to) {
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			copyChild(n, to);
		 }
	}

	@Override
	public void processContents(Node from) {
		copyContents(from, destination);
	}

	protected Node copyChild(Node n, Node inside) {
		Node copy = n.cloneNode(false);
		
		Document ownerDocument = inside.getOwnerDocument();
		ownerDocument.adoptNode(copy);
		
		if (copy instanceof Element) {
			checkIDElement((Element) copy);
		}
		
		inside.appendChild(copy);
		
		copyContents(n, copy);
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
