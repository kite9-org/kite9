package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.format.GraphicsLayer2D;
import org.kite9.diagram.xml.DiagramXMLElement;


public abstract class AbstractDiagramDisplayer extends AbstractADLDisplayer {

	public AbstractDiagramDisplayer(CompleteDisplayer parent, GraphicsLayer2D g2, boolean shadow) {
		super(parent, g2, shadow);
	}
	
	public boolean canDisplay(DiagramElement element) {
		return element instanceof DiagramXMLElement;
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
