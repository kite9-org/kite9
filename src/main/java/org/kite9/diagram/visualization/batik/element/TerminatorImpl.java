package org.kite9.diagram.visualization.batik.element;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Terminator;
import org.kite9.diagram.xml.StyledKite9SVGElement;

public class TerminatorImpl extends AbstractRectangularDiagramElement implements Terminator {

	public TerminatorImpl(StyledKite9SVGElement el, DiagramElement parent) {
		super(el, parent);
	}

	@Override
	protected void initialize() {
	}

}
