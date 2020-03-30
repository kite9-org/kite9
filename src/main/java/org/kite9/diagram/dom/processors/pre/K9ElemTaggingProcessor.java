package org.kite9.diagram.dom.processors.pre;

import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.processors.xpath.AbstractInlineProcessor;
import org.w3c.dom.Element;

/**
 * Tags all elements in the ADL namespace with a k9-elem attribute containing the
 * element name.  This is to make writing CSS rules easier.
 * 
 * @author robmoffat
 *
 */
public class K9ElemTaggingProcessor extends AbstractInlineProcessor {

	public K9ElemTaggingProcessor() {
		super();
	}

	@Override
	protected final Element processTag(Element n) {
		Element out = super.processTag(n);
		if ((!out.hasAttribute("k9-elem")) && (XMLHelper.KITE9_NAMESPACE.equals(out.getNamespaceURI()))) {
			if (out instanceof StyledKite9XMLElement) {
				((StyledKite9XMLElement) out).makeLiveAttribute("k9-elem");
			}
			out.setAttribute("k9-elem", n.getTagName());
		}
		
		out = processTagInner(out);
		
		return out;
	}

	protected Element processTagInner(Element n) {
		return n;
	}

	
}
