package org.kite9.diagram.batik;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.display.AbstractCompleteDisplayer;
import org.kite9.framework.common.Kite9ProcessingException;

public class BatikDisplayer extends AbstractCompleteDisplayer {
	
	public BatikDisplayer(boolean buffer, int gridSize) {
		super(buffer, gridSize);
	}

	protected CostedDimension size(DiagramElement element, Dimension2D within) {
		if (element instanceof SizedRectangular) {
			return ((SizedRectangular) element).getSize(within);
		}

		// not a CompactedRectangular
		return CostedDimension.ZERO;

	}

	/**
	 * Handle scaling before translation, otherwise everything goes whack.
	 */
	@Override
	public void draw(DiagramElement element, RenderingInformation ri){
		throw new Kite9ProcessingException("Unsupported operation");
	}
//		if ((!(element instanceof Decal)) && (ri.getPosition() == null)) {
//			return;	// labels and connected should all have positions by now.
//		} else if ((element instanceof Decal) && ((element.getParent().getRenderingInformation().getSize() == null))) {
//			return; // parents of decals should also be positioned.
//		}
//		
//		if (element instanceof HasSVGGraphics) {
//			DiagramElement parent = element.getParent();
//			HasSVGGraphics hgn = (HasSVGGraphics) element;
//			if (hgn.getGraphicsNode() == null) {
//				return;
//			}
//			
//			GraphicsNode node = hgn.getGraphicsNode();
//
//			if (parent != null) {
//				// make sure the graphics node is anchored to it's parent
//				IdentifiableGraphicsNode parentNode = (IdentifiableGraphicsNode) ((HasSVGGraphics) parent).getGraphicsNode();
//				parentNode.add(node);
//			}
//			
//			
//			Rectangle2D bounds = hgn.getSVGBounds();
//			DiagramElementSizing sizing = getSizing(hgn);
//			
//			if (bounds != null) {				
//				// reset the scale first
//				AffineTransform existing = node.getTransform();
//				AffineTransform global = node.getGlobalTransform();
//				existing.scale(1d/ global.getScaleX(), 1d /global.getScaleY());
//				
//				if (sizing == DiagramElementSizing.FIXED)  {
//					// apply a translation to the Kite9-specified position
//					
//					RectangleRenderingInformation rri = (RectangleRenderingInformation) ri;
//					global = node.getGlobalTransform();
//					existing = node.getTransform();
//					translateRelative(bounds, existing, global, rri);
//				}
//				
//				if ((sizing == DiagramElementSizing.SCALED) || (sizing == DiagramElementSizing.ADAPTIVE)){
//					// applies scale and translation
//					RectangleRenderingInformation rri = (RectangleRenderingInformation) ri;
//					existing = node.getTransform();
//					
//					if (bounds != null) {
//						existing.scale(1d / bounds.getWidth(), 1d/bounds.getHeight());
//						existing.scale(rri.getSize().getWidth(), rri.getSize().getHeight());
//						global = node.getGlobalTransform();
//						translateRelative(bounds, existing, global, rri);
//					}
//				}
//			}
//		}
//	}

	private DiagramElementSizing getSizing(HasSVGGraphics layered) {
		DiagramElementSizing out =  (layered instanceof Rectangular) ?((Rectangular) layered).getSizing() : null;
		return out;
	}

	private void translateRelative(Rectangle2D bounds, AffineTransform existing, AffineTransform global, RectangleRenderingInformation rri) {
		existing.translate(-bounds.getX(), -bounds.getY());
		double xs = global.getScaleX();
		double ys = global.getScaleY();
		double xt = rri.getPosition().x() - global.getTranslateX();
		double yt = rri.getPosition().y() - global.getTranslateY();
		double xst = xt / xs;
		double yst = yt  / ys;
		existing.translate(xst, yst);
	}

	@Override
	public double getPadding(DiagramElement element, Direction d) {
		if (element instanceof SizedRectangular) {
			return ((SizedRectangular) element).getPadding(d);
		} else if (element instanceof Connection) {
			return ((SizedRectangular) element).getPadding(d);
		} else {
			return 0;
		}
	}
	
	@Override
	public double getMargin(DiagramElement element, Direction d) {
		if (element instanceof SizedRectangular) {
			return ((SizedRectangular) element).getMargin(d);
		} else if (element instanceof Connection) {
			return ((Connection) element).getMargin(d);
		} else {
			return 0;
		}
	}

	@Override
	public double getLinkGutter(Rectangular element, Direction d) {
		return 10;
	}

	@Override
	public double getLinkMinimumLength(Connection element) {
		return 10;
	}

	@Override
	public double getTerminatorLength(Terminator terminator) {
		return 5;
	}

	@Override
	public double getTerminatorReserved(Terminator terminator, Connection on) {
		return 5;
	}

	@Override
	public double getLinkInset(Rectangular element, Direction d) {
		return 5;
	}

	@Override
	public boolean requiresHopForVisibility(Connection a, Connection b) {
		return true;
	}

}
