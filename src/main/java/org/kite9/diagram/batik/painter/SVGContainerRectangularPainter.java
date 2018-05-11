package org.kite9.diagram.batik.painter;

import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.framework.xml.StyledKite9SVGElement;

/**
 * Handles painting for {@link DiagramElementType.CONTAINER}
 * 
 * This implementation allows Containers to contain some SVG, but it cannot be used for sizing purposes.
 * 
 * @author robmoffat
 *
 */
public class SVGContainerRectangularPainter extends DirectSVGPainter {

	public SVGContainerRectangularPainter(StyledKite9SVGElement theElement) {
		super(theElement);
	}

}
