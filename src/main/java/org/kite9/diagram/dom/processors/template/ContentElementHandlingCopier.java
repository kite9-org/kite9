package org.kite9.diagram.dom.processors.template;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathFactory;

import org.apache.batik.dom.util.ListNodeList;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.ContentsElement;
import org.kite9.diagram.dom.processors.copier.BasicCopier;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.xpath.XPathResult;

/**
 * Special implementation of the copier which, when it arrives at the <content>
 * element, adds something else in.
 * 
 * @author robmoffat
 *
 */
public class ContentElementHandlingCopier extends BasicCopier {
		
	XPathFactory xPathfactory = XPathFactory.newInstance();
	
	private Node copyOfOriginal;

	/**
	 * As this is a copier, the original content will be entirely replaced by the new, copied content.  
	 * However, for the Content Element to work, we must be able to reference the original content, so
	 * move it all into "copyOfOriginal".
	 */
	public ContentElementHandlingCopier(Node original) {
		super(original);
		this.copyOfOriginal = original.cloneNode(false);
		NodeList in = original.getChildNodes();
		while (in.getLength() > 0) {
			this.copyOfOriginal.appendChild(in.item(0));
		}		
	}

	@Override
	protected Node copyChild(Node n, Node inside) {
		if (n instanceof ContentsElement) {
			ContentsElement contents = (ContentsElement) n;
			if (contents.hasAttribute("xpath")) {
				try {
					ADLDocument doc =(ADLDocument) copyOfOriginal.getOwnerDocument();
					String xpath = contents.getAttribute("xpath");
					XPathResult result = (XPathResult) doc.evaluate(xpath, copyOfOriginal, null, getReturnType(contents), null);
					if (result.getResultType() == XPathResult.STRING_TYPE) {
						String stringValue = result.getStringValue();
						inside.appendChild(copyOfOriginal.getOwnerDocument().createTextNode(stringValue));
					} else {
						List<Node> nodes = new ArrayList<Node>();
						Node node;
	                    while ((node = result.iterateNext()) != null) {
	                        nodes.add(node);
	                    }
						copyContents(new ListNodeList(nodes), inside);
					}
				} catch (Exception e) {
					throw new Kite9ProcessingException(e);
				}
			} else {
				copyContents(copyOfOriginal, inside);
			}
			return null;
		} else {
			return super.copyChild(n, inside);
		}
	}

	private static short getReturnType(ContentsElement contents) {
		if (contents.hasAttribute("type")) {
			String type = contents.getAttribute("type");
			if ("string".equals(type.trim().toLowerCase())) {
				return XPathResult.STRING_TYPE;
			}
		} 
		
		// otherwise assume nodeset
		return XPathResult.ANY_TYPE;
	}
}
