package com.kite9.k9server.command.xml.replace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;

public class ReplaceText extends AbstractReplaceCommand<Element, String> {
	
	public enum PreserveChildElements { BEFORE, AFTER, NONE };
	
	public PreserveChildElements preserve = PreserveChildElements.AFTER;
	
	private List<Element> collectChildren(Element e) {
		if (preserve == PreserveChildElements.NONE) {
			return Collections.emptyList();
		} else {
			List<Element> out = new ArrayList<Element>();
			NodeList nl = e.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				if (nl.item(i) instanceof Element) {
					out.add((Element) nl.item(i)); 
				}
			}
			return out;
		}
	}

	@Override
	protected String getFromContent(ADLDom context) {
		return from;
	}

	@Override
	protected String getToContent(ADLDom context) {
		return to;
	}

	@Override
	protected Element getExistingContent(ADLDom in) {
		Element on = findFragmentElement(in.getDocument(), fragmentId);
		return on;
	}

	@Override
	protected void doReplace(ADLDom adl, Element e, String toContent, String fromContent) {
		List<Element> childElements = collectChildren(e);
		
		e.setTextContent(toContent);
		
		if (preserve == PreserveChildElements.BEFORE) {
			Collections.reverse(childElements);
			childElements.stream().forEach(c -> e.insertBefore(c, null));
		} else if (preserve == PreserveChildElements.AFTER) {
			childElements.stream().forEach(c -> e.appendChild(c));
		}
		
		LOG.info("Processed replace text of "+fragmentId);
	}

	@Override
	protected Mismatch same(Element existing, String with) {
		String eText = existing.getTextContent();
		String eTextReplaced = eText.replaceAll("\\s", "");
		String withReplaced = with.replaceAll("\\s", "");
		switch (preserve) {
		default:
		case AFTER:
			return check(eTextReplaced.startsWith(withReplaced), eText, with);
		case BEFORE:
			return check(eTextReplaced.endsWith(withReplaced), eText, with);
		case NONE:
			return check(eTextReplaced.equals(withReplaced), eText, with);
		}
	}

	private Mismatch check(boolean ok, String eText, String with) {
		return ok ? null : () -> "Text not same: '"+eText+"' and '"+with+"'";
	}
}
