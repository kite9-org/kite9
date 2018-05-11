package org.kite9.diagram.batik.transform;

import org.kite9.diagram.batik.painter.Painter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NoopTransformer implements SVGTransformer {

	@Override
	public Element postProcess(Painter p, Document d) {	
		return p.output(d);
	}

}
