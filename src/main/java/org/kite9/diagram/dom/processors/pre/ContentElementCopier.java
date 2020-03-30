package org.kite9.diagram.dom.processors.pre;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.dom.elements.ADLDocument;
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

/**
 * Handles content-element copying, and also value replacement (stuff inside #{}).
 * However, value-replacement is only done on values beginning with pre:, as usually
 * this is needed in a post-processor scenario.
 * 
 * @author robmoffat
 *
 */
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
			copyNodeList(result, i, contents, isOptional(contents));
		}
	}

	private void copyNodeList(XPathResult result, Node inside, Node source, boolean optional) {
		
		Node node;
		List<Node> toAdd = new ArrayList<>();
		while ((node = result.iterateNext()) != null) {
			toAdd.add(node);
		}
		
		for (Node n : toAdd) {
			processContents(n, inside);
		}
		
		mergeTextNodes(inside.getChildNodes());

		//System.out.println("Copied contents into : "+inside.getLocalName()+ " nodes "+nodes+" with value-replacer "+vr);

		if ((toAdd.isEmpty()) && (!optional)) {
			throw new Kite9XMLProcessingException("XPath returned no value.  Try setting optional='true': ", source);
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
			return (((Attr) n).getValue().startsWith("pre:")); 
		} else {
			return false;
		}
	}

	@Override
	protected void updateAttribute(Element n, Attr a, String newValue) {
		String oldValue = a.getValue();
		newValue = newValue.substring(4);
		a.setNodeValue(newValue);
		if (a.isId()) {
			((ADLDocument) n.getOwnerDocument()).addIdEntry(n, newValue);
			((ADLDocument) n.getOwnerDocument()).removeIdEntry(n, oldValue);
		}
	}

}
