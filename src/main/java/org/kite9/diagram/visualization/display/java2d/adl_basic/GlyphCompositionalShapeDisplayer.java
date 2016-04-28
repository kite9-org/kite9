package org.kite9.diagram.visualization.display.java2d.adl_basic;

import java.awt.Graphics2D;

import org.kite9.diagram.adl.CompositionalShape;
import org.kite9.diagram.adl.Glyph;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.style.BoxStyle;
import org.kite9.diagram.visualization.display.java2d.style.FlexibleShape;
import org.kite9.diagram.visualization.display.java2d.style.Stylesheet;

public class GlyphCompositionalShapeDisplayer extends AbstractBoxModelDisplayer {

	public GlyphCompositionalShapeDisplayer(CompleteDisplayer parent, Graphics2D g2,
			Stylesheet ss, boolean shadow, int xo, int yo) {
		super(parent, g2, ss, shadow, xo, yo);
	}

	@Override
	public BoxStyle getUnderlyingStyle(DiagramElement de) {
		return ss.getGlyphCompositionalShapeStyle();
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
