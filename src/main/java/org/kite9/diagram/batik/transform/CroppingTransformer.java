package org.kite9.diagram.batik.transform;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This makes sure the content is positioned correctly inside it's container.
 */
public class CroppingTransformer extends AbstractRectangularTransformer {

	private Leaf owner;
	
	public CroppingTransformer(Leaf l) {
		this.owner = l;
	}
	
	@Override
	public Element postProcess(Painter p, Document d) {	
		// work out translation
		Dimension2D position = getRenderedRelativePosition(owner);
		
		Element out = p.output(d);
		
		if (p instanceof LeafPainter) {
			Rectangle2D content = ((LeafPainter) p).bounds();
			position = position.minus(new Dimension2D(content.getX(), content.getY()));
			
			if ((position.x() != 0) || (position.y() != 0)) {
				out.setAttribute("transform", "translate(" + position.x() + "," + position.y() + ")");
			}
		}
		
		return out;
	}	

	protected Rectangular getOwner() {
		return owner;
	}
}
