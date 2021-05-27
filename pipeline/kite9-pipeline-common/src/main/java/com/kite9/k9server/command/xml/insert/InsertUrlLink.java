package com.kite9.k9server.command.xml.insert;

import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.ReferencingKite9XMLElement;
import org.w3c.dom.Element;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;

/**
 * Used for inserting links, where we have to say what the link joins to/from.
 * 
 * @author robmoffat
 */
public class InsertUrlLink extends InsertUrl {

	public String fromId, toId;

	protected void updateLinkEnds(Element insert, ADLDom in) {
		ADLDocument doc = (ADLDocument) insert.getOwnerDocument();
		in.ensureCssEngine(doc);
		if (insert instanceof ReferencingKite9XMLElement) {
			((ReferencingKite9XMLElement) insert).setIDReference(CSSConstants.LINK_FROM_XPATH, fromId);
			((ReferencingKite9XMLElement) insert).setIDReference(CSSConstants.LINK_TO_XPATH, toId);
		}
	}

	@Override
	protected void checkProperties() {
		ensureNotNull("fromId", fromId);
		ensureNotNull("toId", toId);
		super.checkProperties();
	}

	@Override
	protected Element getContents(ADLDom in) {
		Element out = super.getContents(in);
		updateLinkEnds(out, in);
		return out;
	}

	
}
