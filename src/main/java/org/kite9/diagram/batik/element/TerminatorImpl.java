package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Terminator;
import org.kite9.framework.xml.StyledKite9SVGElement;

public class TerminatorImpl extends AbstractRectangularDiagramElement implements Terminator {


	public TerminatorImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	@Override
	protected void initialize() {
	}

}
