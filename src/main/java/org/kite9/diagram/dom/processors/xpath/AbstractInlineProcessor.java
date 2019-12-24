package org.kite9.diagram.dom.processors.xpath;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Does without copying, changing the elements in place.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractInlineProcessor extends AbstractProcessor {

	@Override
	public final Node processContents(Node n) {
		return processContents(n, null);
	}

	@Override
	protected Element processTag(Element n) {
		return n;
	}

	@Override
	protected Text processText(Text n) {
		return n;
	}

	@Override
	protected boolean isAppending() {
		return false;
	}
	
	
	
}