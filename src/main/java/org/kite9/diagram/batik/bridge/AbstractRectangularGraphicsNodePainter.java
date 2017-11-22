package org.kite9.diagram.batik.bridge;

import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.model.Rectangular;
import org.kite9.framework.xml.StyledKite9SVGElement;

public abstract class AbstractRectangularGraphicsNodePainter<X extends Rectangular> extends AbstractGraphicsNodePainter<X> implements RectangularPainter<X>{

	public AbstractRectangularGraphicsNodePainter(Kite9BridgeContext ctx) {
		super(ctx);
	}

	@Override
	public Rectangle2D bounds(StyledKite9SVGElement theElement, X l) {
		initializeSourceContents(theElement, l);
		GraphicsNode gn = getGraphicsNode(theElement);
		return gn.getBounds();
	}
}
