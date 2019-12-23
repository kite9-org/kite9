package org.kite9.diagram.dom.processors.copier;

import org.kite9.diagram.dom.processors.xpath.ValueReplacer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Removes namespace prefixes as it goes.  Useful for the final output of SVG.
 * 
 * @author robmoffat
 *
 */
public class PrefixingCopier extends ValueReplacingCopier {
	
	private String newPrefix;
	private String namespace;
	
	public PrefixingCopier(Node destination, ValueReplacer vr, String prefix, String namespace) {
		super(destination, vr);
		this.newPrefix = prefix;
		this.namespace = namespace;
	}

	@Override
	protected Element processTag(Element n) {
		n = super.processTag(n);
		
		if (namespace.equals(n.getNamespaceURI())) {
			n.setPrefix(newPrefix);
		}
		
		NamedNodeMap map = n.getAttributes();
		
		
		int i = 0;
		while (i < map.getLength()) {
			Attr a = (Attr) map.item(i);
			System.out.println(a.getName());
			if ((a.getName().startsWith("xmlns:")) && (a.getValue().equals(namespace))) {
				 map.removeNamedItemNS(a.getNamespaceURI(), a.getLocalName());
			} else {
				i++;
			}
			
		}
		
		return n;
	}

	@Override
	protected boolean canValueReplace(Node n) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Node processContents(Node from) {
		return processContents(from, null);
	}
}
