package org.kite9.diagram.batik.painter;

import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.model.style.DiagramElementType;

/**
 * Handles painting for {@link DiagramElementType.SVG}
 * 
 * @author robmoffat
 *
 */
public class SVGLeafRectangularPainter extends AbstractGraphicsNodePainter implements LeafPainter {
	
	public SVGLeafRectangularPainter(StyledKite9SVGElement theElement, Kite9BridgeContext ctx) {
		super(theElement, ctx);
	}
	
	@Override
	public Rectangle2D bounds() {
		GraphicsNode gn = getGraphicsNode();
		return gn.getBounds();
	}

}
