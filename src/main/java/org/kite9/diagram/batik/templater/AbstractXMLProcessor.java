package org.kite9.diagram.batik.templater;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Contains basic functionality for copying the contents of one XML node 
 * into another.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractXMLProcessor implements XMLProcessor {
	
	public AbstractXMLProcessor() {
	}

	@Override
	public void process(Node from, Node to) {
		 NodeList nl = from.getChildNodes();
		 processNodeList(nl, to);
	}

	protected void processNodeList(NodeList from, Node to) {
		for (int i = 0; i < from.getLength(); i++) {
			Node n = from.item(i);
			Node copy = processNode(n, to);
	
			if (copy != null) {
				process(n, copy);
			}
		}
	}

	protected Node processNode(Node n, Node inside) {
		return n;
	}
}
