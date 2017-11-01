package org.kite9.diagram.batik.templater;

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
	
	public PrefixingCopier(String prefix) {
		super();
		this.newPrefix = prefix;
	}

	@Override
	protected Node processNode(Node n, Node to) {
		Node out =  super.processNode(n, to);
		if (newPrefix != null) {
			changePrefix(out);
		}
		
		return out;
	}

	private void changePrefix(Node copy) {
		if (copy instanceof Element) {
			((Element)copy).setPrefix(newPrefix);
		} 
	}
	
}
