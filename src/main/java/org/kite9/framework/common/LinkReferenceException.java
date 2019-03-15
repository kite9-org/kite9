package org.kite9.framework.common;


public class LinkReferenceException extends Kite9ProcessingException {

	public LinkReferenceException(String r, String id) {
		super("Could not resolve link reference: "+r+" for link "+id);
	}

	private static final long serialVersionUID = -4653211618062874387L;

}
