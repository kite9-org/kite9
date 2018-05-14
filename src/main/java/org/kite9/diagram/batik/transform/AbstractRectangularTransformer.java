package org.kite9.diagram.batik.transform;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.RectangleRenderingInformation;

public abstract class AbstractRectangularTransformer implements SVGTransformer {

	protected Dimension2D getRectangularRenderedSize(DiagramElement de) {
		if (de instanceof Rectangular) {
			RectangleRenderingInformation rri = ((Rectangular) de).getRenderingInformation();
			Dimension2D size = rri.getSize();
			return size;
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the position as an offset from the nearest rectangular parent container. Useful for
	 * translate.
	 */
	protected Dimension2D getRenderedRelativePosition(DiagramElement de) {
		if (de instanceof Rectangular) {
			RectangleRenderingInformation rri = ((Rectangular) de).getRenderingInformation();
			Dimension2D position = rri.getPosition();
			DiagramElement parent = de.getParent();
			while ((parent != null) && (!(parent instanceof Rectangular))) {
				parent = parent.getParent();
			}
			if (parent instanceof Rectangular) {
				rri = ((Rectangular) parent).getRenderingInformation();
				Dimension2D parentPosition = rri.getPosition();
				position = new Dimension2D(position.x() - parentPosition.x(), position.y() - parentPosition.y());
			}
			return position;
		} else {
			return null;
		}
	}
}
