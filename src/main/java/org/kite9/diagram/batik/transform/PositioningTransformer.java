package org.kite9.diagram.batik.transform;

import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.w3c.dom.Element;

/**
 * This makes sure the content is positioned correctly inside it's container.
 * 
 * Content is expected to be defined from 0,0
 * 
 */
public class PositioningTransformer extends AbstractRectangularTransformer {

	private Rectangular owner;
	
	public PositioningTransformer(Rectangular r) {
		this.owner = r;
	}
	
	@Override
	public void postProcess(Element out) {
		// work out translation
		Dimension2D position = getRenderedRelativePosition(owner);
		
		if ((position.x() != 0) || (position.y() != 0)) {
			out.setAttribute("transform", "translate(" + position.x() + "," + position.y() + ")");
		}
	}	

	protected Rectangular getOwner() {
		return owner;
	}
}
