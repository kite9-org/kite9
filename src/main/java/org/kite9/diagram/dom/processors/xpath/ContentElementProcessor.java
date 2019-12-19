package org.kite9.diagram.dom.processors.xpath;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.dom.elements.ContentsElement;
import org.kite9.framework.common.Kite9XMLProcessingException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.xpath.XPathResult;

/**
 * Special implementation of the copier which, when it arrives at the <content>
 * element, adds something else in.
 * 
 * @author robmoffat
 *
 */
public class ContentElementProcessor extends ValueReplacingProcessor {

	public ContentElementProcessor(ValueReplacer vr) {
		super(vr);
	}

	@Override
	public void processElement(Element e) {
		if (e instanceof ContentsElement) {
			processContentsElement((ContentsElement) e);
		} else {
			super.processElement(e);
		}
	}

	protected void processContentsElement(ContentsElement contents) {
		String xpath = "*";
		if (contents.hasAttribute("xpath")) {
			xpath = contents.getAttribute("xpath");
		}

		short returnType = getReturnType(contents);
		XPathResult result = valueReplacer.getReplacementXML(xpath, returnType);
		Node parent = contents.getParentNode();

		if (result.getResultType() == XPathResult.STRING_TYPE) {
			doOptionalCheck(contents, contents, xpath, result.getStringValue());
			Text t = contents.getOwnerDocument().createTextNode(result.getStringValue());
			parent.insertBefore(t, contents);
			processContents(t);
		} else {
			List<Node> nodes = extractNodeList(contents, xpath, result);
			for (Node node : nodes) {
				parent.insertBefore(node, contents);
				processContents(node);
			}	
		}
		
		parent.removeChild(contents);
	}

	private List<Node> extractNodeList(ContentsElement contents, String xpath, XPathResult result) {
		List<Node> nodes = new ArrayList<Node>();
		Node node;
		while ((node = result.iterateNext()) != null) {
			nodes.add(node);
		}

		if (nodes.size() == 0) {
			if (!isOptional(contents)) {
				throw new Kite9XMLProcessingException("XPath returned no value: " + xpath, contents);
			}
		}
		return nodes;
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
}
