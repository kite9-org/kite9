package org.kite9.diagram.batik.format;

import org.kite9.framework.common.Kite9ProcessingException;

public class Kite9TranscoderException extends Kite9ProcessingException {

	private String xml;

	public Kite9TranscoderException(String arg0, Throwable arg1, String xml) {
		super(arg0, arg1);
		this.xml = xml;
	}
	
	
	
}
