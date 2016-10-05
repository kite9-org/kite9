package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.VPos;

/**
 * Provides the Anchor class, which can be used to set the position for a number of
 * {@link DiagramElement}s.
 * @author robmoffat
 *
 */
public abstract class AbstractAnchoringVertex extends AbstractVertex {
	
	protected static class Anchor {
		
		private VPos ud;
		public VPos getUd() {
			return ud;
		}

		public HPos getLr() {
			return lr;
		}

		public DiagramElement getDe() {
			return de;
		}

		private HPos lr;
		private DiagramElement de;

		public Anchor(VPos ud, HPos lr, DiagramElement de) {
			super();
			this.ud = ud;
			this.lr = lr;
			this.de = de;
		}

		private RectangleRenderingInformation getRI() {
			return (RectangleRenderingInformation) de.getRenderingInformation();
		}
	
		public void setX(double x) {
			RectangleRenderingInformation ri = getRI();
			
			if (lr==HPos.LEFT) {
				ri.setPosition(Dimension2D.setX(ri.getPosition(), x));
			} else {
				ri.setSize(Dimension2D.setX(ri.getSize(), x - ri.getPosition().x()));
			}
		}
		
		public void setY(double y) {
			RectangleRenderingInformation ri = getRI();
		
			if (ud==VPos.UP) {
				ri.setPosition(Dimension2D.setY(ri.getPosition(), y));
			} else {
				ri.setSize(Dimension2D.setY(ri.getSize(),y - ri.getPosition().y()));
			}
		}
		
		public String toString() {
			return de.toString()+"-"+lr+"-"+ud; 
		}
	}

	public AbstractAnchoringVertex(String id) {
		super(id);
	}

	


}