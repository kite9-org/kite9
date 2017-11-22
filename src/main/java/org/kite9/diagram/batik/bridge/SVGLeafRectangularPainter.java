package org.kite9.diagram.batik.bridge;

import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles painting for {@link DiagramElementType.SVG}
 * 
 * @author robmoffat
 *
 */
public class SVGLeafRectangularPainter extends AbstractRectangularGraphicsNodePainter<Leaf> implements RectangularPainter<Leaf> {
	
	public SVGLeafRectangularPainter(Kite9BridgeContext ctx) {
		super(ctx);
	}

	@Override
	public Element output(Document d, StyledKite9SVGElement theElement, Leaf r) {
		return outputDirect(d, theElement, r);
	}

}
