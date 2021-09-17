package org.kite9.diagram.dom.processors.copier;

import javax.xml.XMLConstants;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.ns.Kite9Namespaces;
import org.kite9.diagram.dom.processors.AbstractProcessor;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Contains basic functionality for copying the contents of one XML node 
 * into another.
 * 
 * @author robmoffat
 *
 */
public class BasicCopier extends AbstractProcessor {
	
	protected Node destination;
	protected boolean copyTop;
	
	public BasicCopier(Node destination, boolean copyTop) {
		this.destination = destination;
		this.copyTop = copyTop;
	}

	@Override
	protected Element processTag(Element n) {
		Element out = (Element) processNode(n);
		processAttributes(out, n);
		return out;
	}
	
	protected void processAttributes(Element out, Element original) {
	}

	@Override
	protected Text processText(Text n) {
		return (Text) processNode(n);
	}

	@Override
	protected Comment processComment(Comment c) {
		return (Comment) processNode(c);
	}

	private Node processNode(Node n) {
		Node copy = n.cloneNode(false);
		
		Document ownerDocument = getDestinationDocument();
		ownerDocument.adoptNode(copy);
		
		if (copy instanceof Element) {
			removeExtraneousNamespaces((Element) copy);
			checkIDElement((Element) copy);
		}
		
		return copy;
	}

	protected Document getDestinationDocument() {
		return getDocument(destination);
	}

	public static Document getDocument(Node n) {
		return  n instanceof Document ? (Document) n : n.getOwnerDocument();
	}
	
	private void removeExtraneousNamespaces(Element copy) {
		NamedNodeMap nnm = copy.getAttributes();
		
		if (nnm == null) {
			return;
		}
		
		int i = 0;
		while (i < nnm.getLength()) {
			Attr a = (Attr) nnm.item(i);
			String ns = a.getNamespaceURI();
			
			if (shouldRemoveNamespace(ns)) {
				nnm.removeNamedItemNS(ns, a.getLocalName());
			} else {
				i++;
			}
		}
	}

	protected boolean shouldRemoveNamespace(String ns) {
		return (ns != null) && (!ns.equals("")) 
				&& (!ns.equals(SVGConstants.SVG_NAMESPACE_URI)) 
				&& (!ns.equals(Kite9Namespaces.ADL_NAMESPACE))
				&& (!ns.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
				&& (!ns.equals(SVGConstants.XLINK_NAMESPACE_URI))
				&& (!ns.equals(XMLConstants.XML_NS_URI));
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

	@Override
	public Node processContents(Node n) {
		if (copyTop) {
			return super.processContents(n, destination);
		} else {
			if (n instanceof Element) {
				processAttributes((Element) destination, (Element) n);
			}
			NodeList contents = n.getChildNodes();
			mergeTextNodes(contents);
			processNodeList(contents, destination);
			return null;
		}
	}
	
	@Override
	protected boolean isAppending() {
		return true;
	}


}
