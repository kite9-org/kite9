package org.kite9.diagram.dom.painter;

import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.model.style.DiagramElementType;
import org.w3c.dom.Element;

/**
 * Handles painting for {@link DiagramElementType.CONTAINER}
 * 
 * This implementation allows Containers to contain some SVG, but it cannot be used for sizing purposes.
 * 
 * @author robmoffat
 *
 */
public class SVGContainerRectangularPainter extends DirectSVGGroupPainter {

	public SVGContainerRectangularPainter(Element theElement, ElementContext ctx) {
		super(theElement);
	}

}
