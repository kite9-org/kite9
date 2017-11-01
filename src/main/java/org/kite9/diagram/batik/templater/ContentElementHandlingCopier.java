package org.kite9.diagram.batik.templater;

import java.util.ArrayList;
import java.util.List;

import org.kite9.framework.xml.ContentsElement;
import org.w3c.dom.Node;

/**
 * Special implementation of the copier which, when it arrives at the <content>
 * element, adds something else in.
 * 
 * @author robmoffat
 *
 */
public class ContentElementHandlingCopier extends BasicCopier {
	
	private List<Node> contents;

	public ContentElementHandlingCopier(Node contentNode) {
		this.contents = new ArrayList<>(contentNode.getChildNodes().getLength());
		for (int i = 0; i < contentNode.getChildNodes().getLength(); i++) {
			this.contents.add(contentNode.getChildNodes().item(i));
		}
	}

	@Override
	protected Node processNode(Node n, Node inside) {
		if (n instanceof ContentsElement) {
			for (Node node : contents) {
				process(node, inside);
			}
			return null; 
		} else {
			return super.processNode(n, inside);
		}
	}

	
	
}
