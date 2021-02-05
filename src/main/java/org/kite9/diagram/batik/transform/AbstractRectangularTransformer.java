package org.kite9.diagram.batik.transform;

import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.CostedDimension2D;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RenderingInformation;

public abstract class AbstractRectangularTransformer {

	protected Dimension2D getRectangularRenderedSize(DiagramElement de) {
		RenderingInformation ri = de.getRenderingInformation();
		if (ri instanceof RectangleRenderingInformation) {
			Dimension2D size = ((RectangleRenderingInformation)ri).getSize();
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
		Dimension2D position = CostedDimension2D.Companion.getZERO();
		if (de instanceof Decal) {
			return position;
		} else if (de instanceof Rectangular) {
			position = getOrigin(de);
		} 
		
		Dimension2D parentPosition = getParentOrigin(de);
		Dimension2D out = position.minus(parentPosition);
		return out;
	}

	public Dimension2D getOrigin(DiagramElement de) {
		RectangleRenderingInformation rri = ((Rectangular) de).getRenderingInformation();
		Dimension2D position = rri.getPosition();
		
		if (position == null) {
			return CostedDimension2D.Companion.getZERO();
		} else {
			return position;
		}
	}
	
	public Dimension2D getParentOrigin(DiagramElement de) {
		DiagramElement parent = de.getParent();
		while ((parent != null) && (!(parent instanceof Rectangular))) {
			parent = parent.getParent();
		}
		if (parent instanceof Rectangular) {
			RectangleRenderingInformation rri = ((Rectangular) parent).getRenderingInformation();
			Dimension2D parentPosition = rri.getPosition();
			if (parentPosition != null) {
				return parentPosition;
			}
		}
		
		return CostedDimension2D.Companion.getZERO();
	}
}
