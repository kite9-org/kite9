package org.kite9.diagram.dom.processors;

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
	
	public PrefixingCopier(String prefix, Node destination) {
		super(destination);
		this.newPrefix = prefix;
	}

	@Override
	protected Node copyChild(Node n, Node inside) {
		Node out =  super.copyChild(n, inside);
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
