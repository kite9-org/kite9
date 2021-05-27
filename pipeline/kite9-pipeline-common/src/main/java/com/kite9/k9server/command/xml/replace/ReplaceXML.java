package com.kite9.k9server.command.xml.replace;

import org.kite9.diagram.dom.elements.ADLDocument;
import org.springframework.http.HttpStatus;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.command.CommandException;

public class ReplaceXML extends AbstractReplaceCommand<Element, Element> {
	
	@Override
	protected Element getFromContent(ADLDom adl) {
		Element out = decodeElement(from, adl);
		if (!out.getAttribute("id").equals(fragmentId)) {
			throw new CommandException(HttpStatus.BAD_REQUEST, "ReplaceXML should preserve ID");
		}
		return out;
	}

	@Override
	protected Element getToContent(ADLDom adl) {
		Element out = decodeElement(to, adl);
		if (!out.getAttribute("id").equals(fragmentId)) {
			throw new CommandException(HttpStatus.BAD_REQUEST, "ReplaceXML should preserve ID");
		}
		return out;
	}

	@Override
	protected Element getExistingContent(ADLDom in) {
		return findFragmentElement(in.getDocument(), fragmentId);
	}

	@Override
	protected void doReplace(ADLDom in, Element old, Element toContent, Element fromContent) {
		ADLDocument doc = in.getDocument();
		doc.adoptNode(toContent);
		Node into = old.getParentNode();
		into.replaceChild(toContent, old);
		ensureParentElements(into, toContent);
		
		LOG.info("Processed replace XML of "+fragmentId);
	}

	@Override
	protected Mismatch same(Element existing, Element with) {
		return twoElementsAreIdentical(existing, with);
	}

	

}
