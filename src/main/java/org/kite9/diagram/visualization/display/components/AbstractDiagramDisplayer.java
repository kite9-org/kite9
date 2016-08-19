package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.style.DiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.format.GraphicsLayer;
import org.kite9.diagram.xml.Diagram;


public abstract class AbstractDiagramDisplayer extends AbstractADLDisplayer {

	public AbstractDiagramDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
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
