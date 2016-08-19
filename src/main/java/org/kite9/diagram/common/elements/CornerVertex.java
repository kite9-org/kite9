package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.PositionableDiagramElement;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.style.DiagramElement;

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
			ri.setPosition(ri.getPosition().setX(x));
		} else {
			ri.setSize(ri.getSize().setX(x - ri.getPosition().x()));
		}
	}


	private RectangleRenderingInformation getRI() {
		return (RectangleRenderingInformation) ((PositionableDiagramElement)getOriginalUnderlying()).getRenderingInformation();
	}
	
	public void setY(double y) {
		super.setY(y);
		RectangleRenderingInformation ri = getRI();

		if (ud==VPos.UP) {
			ri.setPosition(ri.getPosition().setY(y));
		} else {
			ri.setSize(ri.getSize().setY(y - ri.getPosition().y()));
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
