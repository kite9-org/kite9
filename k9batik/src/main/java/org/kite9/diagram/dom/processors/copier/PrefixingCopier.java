package org.kite9.diagram.dom.processors.copier;

import org.kite9.diagram.dom.processors.xpath.PatternValueReplacer;
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
	
	public PrefixingCopier(Node destination, boolean copyTop, PatternValueReplacer vr, String prefix, String namespace) {
		super(destination, copyTop, vr);
		this.newPrefix = prefix;
		this.namespace = namespace;
	}

	@Override
	protected Element processTag(Element n) {
		n = super.processTag(n);
		
		String oldNs = xmlnsAttr(n.getPrefix());
		String newNs = xmlnsAttr(newPrefix);
		
		if (namespace.equals(n.getNamespaceURI())) {
			n.setPrefix(newPrefix);
		}
		
		NamedNodeMap map = n.getAttributes();
		
		int i = 0;
		while (i < map.getLength()) {
			Attr a = (Attr) map.item(i);
			if (oldNs.equals(a.getName())) {
				int atts = map.getLength();
				 map.removeNamedItemNS(a.getNamespaceURI(), a.getLocalName());
				 if (map.getLength() == atts) {
					 // can't remove since it's a default attribute.
					 i++;
				 }
			} else if (newNs.equals(a.getName())) {
				a.setValue(this.namespace);
				i++;
			} else {
				i++;
			}
		}
		
		return n;
	}
	
	private static String xmlnsAttr(String prefix) {
		return ((prefix == null) || (prefix.length() == 0)) ? "xmlns" : "xmlns:" + prefix;
	}

	@Override
	protected boolean canValueReplace(Node n) {
		// TODO Auto-generated method stub
		return false;
	}

}
