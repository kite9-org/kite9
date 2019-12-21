package org.kite9.diagram.batik.transform;

import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NoopTransformer implements SVGTransformer {

	@Override
	public Element postProcess(Painter p, Document d, XMLProcessor posProcessor) {	
		return p.output(d, posProcessor);
	}

}
