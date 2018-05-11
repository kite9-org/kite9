package org.kite9.diagram.batik.transform;

import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.RectangleRenderingInformation;

public abstract class AbstractRectangularTransformer implements SVGTransformer {

	protected Dimension2D getRectangularRenderedSize(Rectangular de) {
		if (de instanceof Decal) {
			// return parent size
			de = de.getContainer();
		}
		
		RectangleRenderingInformation rri = de.getRenderingInformation();
		Dimension2D size = rri.getSize();
		return size;
	}
	
	/**
	 * Returns the position as an offset from the parent container.  Useful for translate.
	 */
	protected Dimension2D getRenderedRelativePosition(Rectangular de) {
		RectangleRenderingInformation rri = de.getRenderingInformation();
		Dimension2D position = rri.getPosition();
		if (de.getParent() instanceof Container) {
			rri = ((Container) de.getParent()).getRenderingInformation();
			Dimension2D parentPosition = rri.getPosition();
			position = new Dimension2D(position.x() - parentPosition.x(), position.y() - parentPosition.y());
		}
		return position;
	}
}
