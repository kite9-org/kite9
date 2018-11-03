package org.kite9.diagram.batik.painter;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.painter.DirectSVGGroupPainter;
import org.kite9.diagram.model.style.DiagramElementType;

/**
 * Handles painting for {@link DiagramElementType.CONTAINER}
 * 
 * This implementation allows Containers to contain some SVG, but it cannot be used for sizing purposes.
 * 
 * @author robmoffat
 *
 */
public class SVGContainerRectangularPainter extends DirectSVGGroupPainter {

	public SVGContainerRectangularPainter(StyledKite9SVGElement theElement, Kite9BridgeContext ctx) {
		super(theElement, ctx);
	}

}
