package org.kite9.diagram.common.elements.vertex;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.HPos;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.VPos;
import org.kite9.framework.logging.LogicException;

/**
 * Provides the Anchor class, which can be used to set the position for a number of
 * {@link DiagramElement}s.
 * @author robmoffat
 *
 */
public abstract class AbstractAnchoringVertex extends AbstractVertex {
	
	public static class Anchor {
		
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
			try {
				if (de == null) {
					return;
				}
				RectangleRenderingInformation ri = getRI();
				double l = ri.getPosition() == null ? 0 : ri.getPosition().x();
				double r = ri.getSize() == null ? l : l+ri.getSize().getWidth();
				double u = ri.getPosition() == null ? 0 : ri.getPosition().y();
				double d = ri.getSize() == null ? u : u+ri.getSize().getHeight();
				
				if (lr==HPos.LEFT) {
					l = x;
				} else if (lr==HPos.RIGHT){
					r = x;
				}

				ri.setPosition(new Dimension2D(l, u));
				ri.setSize(new Dimension2D(r-l, d-u));
				

			} catch (NullPointerException e) {
				throw new LogicException("NPE setting position of "+this, e);
			}
		}
		
		public void setY(double y) {
			try {
				if (de == null) {
					return;
				}
				RectangleRenderingInformation ri = getRI();
				double l = ri.getPosition() == null ? 0 : ri.getPosition().x();
				double r = ri.getSize() == null ? l : l+ri.getSize().getWidth();
				double u = ri.getPosition() == null ? 0 : ri.getPosition().y();
				double d = ri.getSize() == null ? u : u+ri.getSize().getHeight();
			
				if (ud==VPos.UP) {
					u = y;
				} else if (ud == VPos.DOWN){
					d = y;
				}
				
				ri.setPosition(new Dimension2D(l, u));
				ri.setSize(new Dimension2D(r-l, d-u));

				
				
			} catch (NullPointerException e) {
				throw new LogicException("NPE setting position of "+this, e);
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