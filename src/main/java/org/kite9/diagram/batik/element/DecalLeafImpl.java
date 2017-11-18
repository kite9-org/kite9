package org.kite9.diagram.batik.element;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.RectangularPainter;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

public class DecalLeafImpl extends AbstractRectangularDiagramElement implements Decal, Leaf {

	public DecalLeafImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<Leaf> lo) {
		super(el, parent, ctx, lo);
	}
	
	@Override
	protected void initSizing() {
		super.initSizing();
		if (this.sizing != DiagramElementSizing.ADAPTIVE) {
			this.sizing = DiagramElementSizing.SCALED;		 
		}
	}

	@Override
	public DiagramElementSizing getSizing() {
		ensureInitialized();
		return sizing;
	}

	@Override
	public Rectangle2D getSVGBounds() {
		ensureInitialized();
		return ((RectangularPainter<?>) this.p).bounds(theElement);
	}

	@Override
	protected Element postProcess(Element out) {
		// special decal sizing options...
		return out;
	}

	
}
