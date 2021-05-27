package com.kite9.k9server.command.xml.replace;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kite9.diagram.dom.elements.ADLDocument;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;

/**
 * Replaces some tag names and attributes of tags, but keeps contents unaltered.
 * 
 * @author robmoffat
 *
 */
public class ReplaceTag extends AbstractReplaceCommand<Element, Element> {

	public List<String> keptAttributes = Arrays.asList("id");	// just keep ID by default.
	public List<String> keptTags = Collections.emptyList();
	
	private void checkKeptTags(Element e, Element n, Document d) {
		if (keptTags.contains(e.getTagName())) {
			d.renameNode(n, e.getNamespaceURI(), e.getLocalName());
		}
	}

	private void keepImportantAttributes(Element e, Element n) {
		for (String a : keptAttributes) {
			if (e.hasAttribute(a)) {
				n.setAttribute(a, e.getAttribute(a));
			}
		}
	}

	private void copyAttributes(Element from, Element to, boolean matching) {
		for (int i = 0; i < from.getAttributes().getLength(); i++) {
			Attr item = (Attr) from.getAttributes().item(i);
			if (matching == (keptAttributes.contains(item.getNodeName()))) {
				to.setAttribute(item.getNodeName(), item.getNodeValue());
			}
		}
	}

	private void moveContents(Element from, Element to) {
		NodeList toNodes = to.getChildNodes();
		while (toNodes.getLength() > 0) {
			to.removeChild(toNodes.item(0));
		}
		
		NodeList fromNodes = from.getChildNodes();
		while (fromNodes.getLength() > 0) {
			to.appendChild(fromNodes.item(0));
		}
	}
	
	@Override
	protected void doReplace(ADLDom in, Element e, Element n, Element fromContent) {
		ADLDocument doc = in.getDocument();
		doc.adoptNode(n);
		keepImportantAttributes(e, n);
		e.getParentNode().insertBefore(n, e);
		ensureParentElements(e.getParentNode(), n);
		copyAttributes(e, n, true);
		checkKeptTags(e, n, doc);
		moveContents(e, n);
		e.getParentNode().removeChild(e);
		
		LOG.info("Processed replace tag of "+fragmentId);
	}

	@Override
	protected Mismatch same(Element existing, Element with) {
		Mismatch m1 = checkAttributesSame(existing, with);
		return m1 == null ? checkAttributesSame(with, existing) : m1;
	}

	private Mismatch checkAttributesSame(Element a, Element b) {
		NamedNodeMap nnm = a.getAttributes();
		for (int i = 0; i < nnm.getLength(); i++) {
			Attr aa = (Attr) nnm.item(i);
			
			if ((!keptAttributes.contains(aa.getName())) && (!"xmlns".equals(aa.getName()))) {
				String aValue = aa.getValue();
				String bValue = b.getAttribute(aa.getName());
				if (!aValue.equals(bValue)) {
					return () -> "Attribute" + aa.getName()+" differs: "+aValue+" and "+bValue;
				}
			}	
		}
		return null;
	}
	
	@Override
	protected Element getFromContent(ADLDom adl) {
		Element out = decodeElement(from, adl);
		return out;
	}

	@Override
	protected Element getToContent(ADLDom adl) {
		Element out = decodeElement(to, adl);
		return out;
	}

	@Override
	protected Element getExistingContent(ADLDom in) {
		return findFragmentElement(in.getDocument(), fragmentId);
	}
}
