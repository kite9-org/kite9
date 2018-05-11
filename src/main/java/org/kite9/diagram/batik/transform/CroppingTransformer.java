package org.kite9.diagram.batik.transform;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Dimension2D;
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
	public void postProcess(Element out) {
		// work out translation
		Dimension2D position = getRenderedRelativePosition(owner);
		
		Rectangle2D content = owner.getBounds();
		position = position.minus(new Dimension2D(content.getX(), content.getY()));
		
		if ((position.x() != 0) || (position.y() != 0)) {
			out.setAttribute("transform", "translate(" + position.x() + "," + position.y() + ")");
		}
	}	

	protected Rectangular getOwner() {
		return owner;
	}
}
