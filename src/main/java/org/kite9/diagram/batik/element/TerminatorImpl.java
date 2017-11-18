package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.RectangularPainter;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.Terminator;
import org.kite9.framework.xml.StyledKite9SVGElement;

public class TerminatorImpl extends AbstractRectangularDiagramElement implements Terminator {

	public TerminatorImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<Leaf> rp) {
		super(el, parent, ctx, rp);
	}

	@Override
	protected void initialize() {
	}

}
