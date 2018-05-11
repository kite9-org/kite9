package org.kite9.diagram.dom.processors;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.kite9.diagram.dom.elements.ContentsElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	 * This empties out the original as it goes, moving all it's children into a copy (for now).
	 */
	public ContentElementHandlingCopier(Node original) {
		super(original);
		this.copyOfOriginal = original.cloneNode(false);  //original.getOwnerDocument().createElement(original.getNodeName());
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
					XPath xpath = xPathfactory.newXPath();
					XPathExpression expr = xpath.compile(contents.getAttribute("xpath"));
					Object result = expr.evaluate(copyOfOriginal, getReturnType(contents));
					if (result instanceof NodeList) {
						NodeList subset = (NodeList) result;
						copyContents(subset, inside);
					} else {
						String stringValue = result.toString();
						inside.appendChild(copyOfOriginal.getOwnerDocument().createTextNode(stringValue));
					}
				} catch (XPathExpressionException e) {
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

	private QName getReturnType(ContentsElement contents) {
		if (contents.hasAttribute("type")) {
			String type = contents.getAttribute("type");
			if ("string".equals(type.trim().toLowerCase())) {
				return XPathConstants.STRING;
			}
		} 
		
		// otherwise assume nodeset
		return XPathConstants.NODESET;
	}
}
