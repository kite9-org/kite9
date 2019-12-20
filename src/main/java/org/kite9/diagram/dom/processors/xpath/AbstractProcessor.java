package org.kite9.diagram.dom.processors.xpath;

import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public abstract class AbstractProcessor implements XMLProcessor {

	@Override
	public final void processContents(Node n) {
		if (n instanceof Element) {
			processTag((Element) n);
			NodeList contents = n.getChildNodes();
			mergeTextNodes(contents);
			processElementContents(contents);
		} else if (n instanceof Text) {
			processText((Text) n);
		}
	}

	protected void processTag(Element n) {
	}

	protected void processText(Text n) {
	}

	protected void processElementContents(NodeList contents) {
		for (int i = 0; i < contents.getLength(); i++) {
			Node item = contents.item(i);
			if (item instanceof Kite9XMLElement) {
				// leave elements to process their own content
			} else {
				processContents(item);
			}
		}
	}

	private void mergeTextNodes(NodeList nodeList) {
		Text lastTextNode = null;
		int i = 0;
		while (i < nodeList.getLength()) {
			Node n = nodeList.item(i);
			if (n instanceof Text) {
				if (lastTextNode != null) {
					lastTextNode.setData(lastTextNode.getData() + ((Text)n).getData());
					n.getParentNode().removeChild(n);
				} else {
					i++;
				}
			} else {
				lastTextNode = null;
				i++;
			}
		}
	}

}