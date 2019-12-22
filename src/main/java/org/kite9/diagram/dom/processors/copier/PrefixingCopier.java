package org.kite9.diagram.dom.processors.copier;

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
public class PrefixingCopier extends BasicCopier {
	
	private String newPrefix;
	private String namespace;
	
	public PrefixingCopier(Node destination, String prefix, String namespace) {
		super(destination);
		this.newPrefix = prefix;
		this.namespace = namespace;
	}

	@Override
	protected Element processTag(Element n) {
		n = super.processTag(n);
		n.setPrefix(newPrefix);
		
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

//	protected boolean shouldRemoveNamespace(String ns) {
//		boolean out = super.shouldRemoveNamespace(ns);
//		boolean local = (ns != null) && (!ns.equals("")) && (!ns.equals(namespace));
//		return local || out;
//	}
//	
}
