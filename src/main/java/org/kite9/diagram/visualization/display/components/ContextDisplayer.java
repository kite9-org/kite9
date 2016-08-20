package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.Context;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Label;
import org.kite9.diagram.adl.StyledDiagramElement;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.format.GraphicsLayer;


public class ContextDisplayer extends AbstractBoxModelDisplayer {

	public ContextDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	public boolean canDisplay(DiagramElement element) {
		return element instanceof Context;
	}
	
	@Override
	public BoxStyle getUnderlyingStyle(DiagramElement de) {
		return new BoxStyle((StyledDiagramElement) de);
	}

	/**
	 * This is called if the container is empty.
	 */
	@Override
	protected Dimension2D sizeBoxContents(DiagramElement element, Dimension2D within) {
		Context c = (Context) element;
		Label l = c.getLabel();
		if (l == null) {
			return null;
		} else {
			return parent.size(l, within);
		}
	}

	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return AbstractBoxModelDisplayer.DEFAULT_SHAPE;
	}
}
