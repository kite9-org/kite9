package org.kite9.framework.common;

import org.w3c.dom.Document;

public class LinkReferenceException extends Kite9XMLProcessingException {

	public LinkReferenceException(String r, String id, Document e) {
		super("Could not resolve link reference: "+r+" for link "+id, null, e);
	}

	private static final long serialVersionUID = -4653211618062874387L;

}
