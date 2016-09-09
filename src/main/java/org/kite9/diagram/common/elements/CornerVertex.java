package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.VPos;

/**
 * During the orthogonalization process, an underlying rectangular diagram element is given dimension, and therefore
 * it's shape is modelled by four {@link CornerVertex} objects at the corners.
 */
public class CornerVertex extends AbstractVertex {

	VPos ud;
	
	HPos lr;
		
	public CornerVertex(String name, HPos lr, VPos ud, DiagramElement underlying) {
		super(name);
		this.lr = lr;
		this.ud = ud;
		this.originalUnderlying = underlying;
	}

	
	public void setX(double x) {
		super.setX(x);
		RectangleRenderingInformation ri = getRI();
		
		if (lr==HPos.LEFT) {
			ri.setPosition(Dimension2D.setX(ri.getPosition(), x));
		} else {
			ri.setSize(Dimension2D.setX(ri.getSize(), x - ri.getPosition().x()));
		}
	}


	private RectangleRenderingInformation getRI() {
		return (RectangleRenderingInformation) getOriginalUnderlying().getRenderingInformation();
	}
	
	public void setY(double y) {
		super.setY(y);
		RectangleRenderingInformation ri = getRI();

		if (ud==VPos.UP) {
			ri.setPosition(Dimension2D.setY(ri.getPosition(), y));
		} else {
			ri.setSize(Dimension2D.setY(ri.getSize(),y - ri.getPosition().y()));
		}
	}

	public HPos getLr() {
		return lr;
	}

	public void setLr(HPos lr) {
		this.lr = lr;
	}

	public VPos getUd() {
		return ud;
	}

	DiagramElement originalUnderlying;

	public DiagramElement getOriginalUnderlying() {
		return originalUnderlying;
	}
}
