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
public class SVGLeafPainter extends AbstractGraphicsNodePainter implements LeafPainter {
	
	public SVGLeafPainter(StyledKite9SVGElement theElement, Kite9BridgeContext ctx) {
		super(theElement, ctx);
	}
	
	@Override
	public Rectangle2D bounds() {
		GraphicsNode gn = getGraphicsNode();
		Rectangle2D drawnBounds = gn.getBounds();
		Rectangle2D diagramElementBounds = new Rectangle2D.Double(0, 0, drawnBounds.getMaxX(), drawnBounds.getMaxY());
		return diagramElementBounds;
	}

}
