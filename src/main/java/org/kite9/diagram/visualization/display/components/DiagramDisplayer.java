package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.PositionableDiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.Stylesheet;
import org.kite9.diagram.visualization.format.GraphicsLayer;


public class DiagramDisplayer extends AbstractADLDisplayer {

	public DiagramDisplayer(CompleteDisplayer parent, Stylesheet ss, GraphicsLayer g2, boolean shadow, int xo, int yo) {
		super(parent, g2, ss, shadow, xo, yo);
	}

	public void draw(DiagramElement element, RenderingInformation ri) {
		// nothing to draw directly for the diagram, all drawing is done for child attr
		// TODO: perhaps add the watermark?
		// TODO: add the URL from the key
		((PositionableDiagramElement)element).setRenderingInformation(ri);
	}
	
	public boolean canDisplay(DiagramElement element) {
		return element instanceof Diagram;
	}

	@Override
	public double getLinkMargin(DiagramElement de, Direction d) {
		return 0;
	}

	@Override
	public double getPadding(DiagramElement element, Direction d) {
		return 0;
	}
	
}
