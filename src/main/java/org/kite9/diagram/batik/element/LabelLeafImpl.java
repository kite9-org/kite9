package org.kite9.diagram.batik.element;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.RectangularPainter;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.framework.xml.StyledKite9SVGElement;

/**
 * For text or shape-based labels within the diagram.
 * 
 * @author robmoffat
 * 
 */
public class LabelLeafImpl extends AbstractLabelImpl implements Label, Leaf {
	
	public LabelLeafImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<Leaf> lo) {
		super(el, parent, ctx, lo);
	}

	@Override
	public boolean hasContent() {
		return !getText().isEmpty();
	}
	
	public String getText() {
		ensureInitialized();
		return theElement.getTextContent().trim();
	}
	
	@Override
	public Rectangle2D getSVGBounds() {
		ensureInitialized();
		return ((RectangularPainter) this.p).bounds(theElement);
	}
	
	@Override
	public CostedDimension getSize(Dimension2D within) {
		Rectangle2D bounds = this.getSVGBounds();
		if (bounds == null) {
			return new CostedDimension(1, 1, 0);
		}
		return new CostedDimension(bounds.getWidth(), bounds.getHeight(), within);
	} 
	
}