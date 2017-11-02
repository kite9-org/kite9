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

	public ContentElementHandlingCopier(List<Node> contents, Node destination) {
		super(destination);
		this.contents = contents;
	}

	@Override
	protected Node copyChild(Node n, Node inside) {
		if (n instanceof ContentsElement) {
			for (Node node : contents) {
				copyChild(node, inside);
			}
			return null; 
		} else {
			return super.copyChild(n, inside);
		}
	}

	
	
}
