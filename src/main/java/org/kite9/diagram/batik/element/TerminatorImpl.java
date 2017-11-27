package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Painter;
import org.kite9.diagram.batik.bridge.RectangularPainter;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.Terminator;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TerminatorImpl extends AbstractRectangularDiagramElement implements Terminator {

	public TerminatorImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<Leaf> rp) {
		super(el, parent, ctx, rp);
	}

	@Override
	protected void initialize() {
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Element output(Document d) {
//		ensureInitialized();
//		preProcess(theElement);
//		Element out = ((Painter<DiagramElement>)p).output(d, theElement, this);
//		postProcess(out);
//		return out;
		return null; 	// nothing output for terms yet
	}
}
