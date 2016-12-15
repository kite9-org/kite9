package org.kite9.diagram.visualization.display.components;

import org.kite9.diagram.adl.CompositionalShape;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.adl.StyledDiagramElement;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.BoxStyle;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.format.GraphicsLayer;

public class GlyphCompositionalShapeDisplayer extends AbstractRectangularDiagramElementDisplayer {

	public GlyphCompositionalShapeDisplayer(CompleteDisplayer parent, GraphicsLayer g2, boolean shadow) {
		super(parent, g2, shadow);
	}

	@Override
	public BoxStyle getUnderlyingStyle(DiagramElement de) {
		return new BoxStyle((StyledDiagramElement) de);
	}

	@Override
	public boolean canDisplay(DiagramElement element) {
		return (element instanceof CompositionalShape) && (((CompositionalShape)element).getParent() instanceof Glyph);
	}

	@Override
	protected Dimension2D sizeBoxContents(DiagramElement element,
			Dimension2D within) {
		return CostedDimension.ZERO;
	}

	@Override
	protected FlexibleShape getDefaultBorderShape(DiagramElement de) {
		return null;
	}

	@Override
	public void draw(DiagramElement element, RenderingInformation r) {
		super.draw(element, r);
	}
	
	
}
