package org.kite9.diagram.dom.processors.copier;

import javax.xml.XMLConstants;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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
		System.out.println("BasicCopier: "+from.getLocalName()+" "+to.getLocalName());
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
		
		removeExtraneousNamespaces(copy);
		
		Document ownerDocument = inside.getOwnerDocument();
		ownerDocument.adoptNode(copy);
		
		if (copy instanceof Element) {
			checkIDElement((Element) copy);
		}
		
		inside.appendChild(copy);
		
		copyContents(n, copy);
		return copy;
	}

	private void removeExtraneousNamespaces(Node copy) {
		NamedNodeMap nnm = copy.getAttributes();
		
		if (nnm == null) {
			return;
		}
		
		int i = 0;
		while (i < nnm.getLength()) {
			Attr a = (Attr) nnm.item(i);
			String ns = a.getNamespaceURI();
			
			if ((ns != null) && (!ns.equals("")) 
					&& (!ns.equals(SVGConstants.SVG_NAMESPACE_URI)) 
					&& (!ns.equals(XMLHelper.KITE9_NAMESPACE))
					&& (!ns.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
					&& (!ns.equals(SVGConstants.XLINK_NAMESPACE_URI))
					&& (!ns.equals(XMLConstants.XML_NS_URI))) {
				nnm.removeNamedItemNS(ns, a.getLocalName());
			} else {
				i++;
			}
		}
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
