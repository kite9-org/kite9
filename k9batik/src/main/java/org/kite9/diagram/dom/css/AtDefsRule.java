package org.kite9.diagram.dom.css;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.Rule;

public class AtDefsRule implements Rule {

	private String uri;
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public AtDefsRule(String uri) {
		this.uri = uri;
	}

	@Override
	public short getType() {
		return 21;
	}

	@Override
	public String toString(CSSEngine eng) {
		return "@defs url("+uri+");";
	}

}
