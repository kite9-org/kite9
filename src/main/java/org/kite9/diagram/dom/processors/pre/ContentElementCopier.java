package org.kite9.diagram.dom.processors.pre;

import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.elements.ContentsElement;
import org.kite9.diagram.dom.processors.copier.ValueReplacingCopier;
import org.kite9.diagram.dom.processors.xpath.ValueReplacer;
import org.kite9.framework.common.Kite9XMLProcessingException;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.xpath.XPathResult;

public class ContentElementCopier extends ValueReplacingCopier {

	public ContentElementCopier(Node destination, ValueReplacer vr) {
		super(destination, false, vr);
	}

	@Override
	protected void processContents(NodeList contents, Node inside) {
		for (int i = 0; i < contents.getLength(); i++) {
			Node n = contents.item(i);
			if (n instanceof ContentsElement) {
				processContentsElement((ContentsElement) n, inside);
			} else {
				processContents(n, inside);
			}
		}
	}
	
	protected void processContentsElement(ContentsElement contents, Node i) {
		String xpath = "child::node()";
		if (contents.hasAttribute("xpath")) {
			xpath = contents.getAttribute("xpath");
		}

		short returnType = getReturnType(contents);
		XPathResult result = vr.getReplacementXML(xpath, returnType, contents);

		if (result.getResultType() == XPathResult.STRING_TYPE) {
			doOptionalCheck(contents, contents, xpath, result.getStringValue());
			Text t = contents.getOwnerDocument().createTextNode(result.getStringValue());
			processContents(t, i);
		} else {
			copyNodeList(xpath, result, i, contents, isOptional(contents));
		}
	}

	private void copyNodeList(String xpath, XPathResult result, Node inside, Node source, boolean optional) {
		int nodes = 0;
		
		Node node;
		while ((node = result.iterateNext()) != null) {
			processContents(node, inside);
			nodes++;
		}

		System.out.println("Copied contents into : "+inside.getLocalName()+ " nodes "+nodes+" with value-replacer "+vr);

		if ((nodes==0) && (!optional)) {
			throw new Kite9XMLProcessingException("XPath returned no value: " + xpath, source);
		}
	}

	private void doOptionalCheck(Element n, ContentsElement contents, String xpath, String stringValue) {
		if ("".equals(stringValue)) {
			if (!isOptional(contents)) {
				throw new Kite9XMLProcessingException("XPath returned no value: " + xpath, n);
			}
		}
	}

	private static boolean isOptional(ContentsElement contents) {
		if (contents.hasAttribute("optional")) {
			String type = contents.getAttribute("optional");
			if ("true".equals(type.trim().toLowerCase())) {
				return true;
			}
		}

		return false;
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

	@Override
	protected boolean canValueReplace(Node n) {
		if (n instanceof Attr) {
			return XMLHelper.PREPROCESSOR_NAMESPACE.equals(n.getNamespaceURI());
		} else {
			return false;
		}
	}

	@Override
	protected void updateAttribute(Element n, Attr a, String newValue) {
		String localName = a.getLocalName();
		n.removeAttributeNode(a);
		n.setAttribute(localName, newValue);
	}
	

	

	
}
