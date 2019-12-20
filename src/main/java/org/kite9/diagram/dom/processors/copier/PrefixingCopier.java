package org.kite9.diagram.dom.processors.copier;

import org.kite9.diagram.dom.processors.xpath.ValueReplacer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Removes namespace prefixes as it goes.  Useful for the final output of SVG.
 * 
 * @author robmoffat
 *
 */
public class PrefixingCopier extends ValueReplacingCopier {
	
	private String newPrefix;
	
	public PrefixingCopier(Node destination, ValueReplacer vr, String prefix) {
		super(destination, vr);
		this.newPrefix = prefix;
	}

	@Override
	protected Element processTag(Element n) {
		n = super.processTag(n);
		n.setPrefix(newPrefix);
		return n;
	}

	@Override
	protected boolean canValueReplace(Node n) {
		return true;
	}
	
}
