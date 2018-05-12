package org.kite9.diagram.dom.processors;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public abstract class AbstractProcessor implements XMLProcessor {

	@Override
	public void processContents(Node n) {
		if (n instanceof Element) {
			processElement((Element) n);
		} else if (n instanceof Text) {
			processText((Text) n);
		}
		
		NodeList contents = n.getChildNodes();
		for (int i = 0; i < contents.getLength(); i++) {
			processContents(contents.item(i));
		}
	}

	protected void processText(Text n) {
	}

	protected void processElement(Element n) {
	}

}
