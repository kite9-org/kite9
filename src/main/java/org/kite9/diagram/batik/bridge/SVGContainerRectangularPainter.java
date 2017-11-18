package org.kite9.diagram.batik.bridge;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Element;

/**
 * Handles painting for {@link DiagramElementType.CONTAINER}
 * 
 * This implementation allows Containers to contain some SVG, but it cannot be used for sizing purposes.
 * 
 * @author robmoffat
 *
 */
public class SVGContainerRectangularPainter extends AbstractSVGPainter<Container> implements RectangularPainter<Container> {

	/**
	 * Bounds for rectangles is special - it should be the minimum size possible.
	 */
	@Override
	public Rectangle2D bounds(Element in) {
		throw new Kite9ProcessingException("No fixed bounds for Container, it depends on contents: " + in);
	}

}
