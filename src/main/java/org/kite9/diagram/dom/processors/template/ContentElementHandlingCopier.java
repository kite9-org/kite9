package org.kite9.diagram.dom.processors.template;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.kite9.diagram.dom.elements.ContentsElement;
import org.kite9.diagram.dom.processors.copier.BasicCopier;
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
	private List<String> parameters;

	/**
	 * This empties out the original as it goes, moving all it's children into a copy (for now).
	 * @param parameters 
	 */
	public ContentElementHandlingCopier(Node original, List<String> parameters) {
		super(original);
		this.parameters = parameters;
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
					xpath.setXPathVariableResolver(new XPathVariableResolver() {
						
						@Override
						public Object resolveVariable(QName arg0) {
							String local = arg0.getLocalPart();
							try {
								int arg = Integer.parseInt(local);
								return parameters.get(arg-1);
							} catch (Exception e) {
								throw new Kite9ProcessingException("Couldn't resolve xpath parameter: "+arg0, e);
							}
						}
					});
					
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
