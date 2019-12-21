package org.kite9.diagram.dom.processors.copier;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Removes namespace prefixes as it goes.  Useful for the final output of SVG.
 * 
 * @author robmoffat
 *
 */
public class PrefixingCopier extends BasicCopier {
	
	private String newPrefix;
	
	public PrefixingCopier(Node destination, String prefix) {
		super(destination);
		this.newPrefix = prefix;
	}

	@Override
	protected Element processTag(Element n) {
		n = super.processTag(n);
		n.setPrefix(newPrefix);
		return n;
	}

}
