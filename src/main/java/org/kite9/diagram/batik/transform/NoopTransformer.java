package org.kite9.diagram.batik.transform;

import org.w3c.dom.Element;

public class NoopTransformer implements SVGTransformer {

	@Override
	public void postProcess(Element out) {
	}

}
