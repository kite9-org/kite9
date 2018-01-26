package org.kite9.diagram.batik.painter;

import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.framework.xml.StyledKite9SVGElement;

/**
 * Handles painting for {@link DiagramElementType.SVG}
 * 
 * @author robmoffat
 *
 */
public class SVGLeafRectangularPainter extends AbstractGraphicsNodePainter<Leaf> implements RectangularPainter<Leaf> {
	
	public SVGLeafRectangularPainter(Kite9BridgeContext ctx) {
		super(ctx);
	}
	
	@Override
	public Rectangle2D bounds(StyledKite9SVGElement theElement, Leaf l) {
		GraphicsNode gn = getGraphicsNode(getContents(theElement, l));
		return gn.getBounds();
	}

}
