package org.kite9.diagram.batik.transform;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This makes sure the content is positioned correctly inside it's container.
 * 
 * Content is expected to be defined from 0,0
 * 
 */
public class PositioningTransformer extends AbstractRectangularTransformer implements LeafTransformer {

	private Rectangular owner;
	
	public PositioningTransformer(Rectangular r) {
		this.owner = r;
	}
	
	@Override
	public Element postProcess(Painter p, Document d) {	
		// work out translation
		Dimension2D position = getRenderedRelativePosition(owner);
		Element out = p.output(d);
		
		if ((position.x() != 0) || (position.y() != 0)) {
			out.setAttribute("transform", "translate(" + position.x() + "," + position.y() + ")");
		}
		
		return out;
	}

	@Override
	public Dimension2D getBounds(LeafPainter p) {
		Rectangle2D r = p.bounds();
		return new Dimension2D(r.getMaxX(), r.getMaxY());
	}	
}
