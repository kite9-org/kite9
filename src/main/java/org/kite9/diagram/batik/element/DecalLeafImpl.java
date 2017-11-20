package org.kite9.diagram.batik.element;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.RectangularPainter;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

public class DecalLeafImpl extends AbstractRectangularDiagramElement implements Decal, Leaf {

	public DecalLeafImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<Leaf> lo) {
		super(el, parent, ctx, lo);
	}

	@Override
	public Rectangle2D getBounds() {
		throw new Kite9ProcessingException("Decal doesn't have bounds");
	}

	
	/**
	 * Decal size is based on it's parent element, since it doesn't have a computed size of it's own.
	 */
	@Override
	protected void preProcessSize(StyledKite9SVGElement out) {
		RectangleRenderingInformation parentRRI = (RectangleRenderingInformation) getParent().getRenderingInformation();
		double width = parentRRI.getSize().getWidth();
		double height = parentRRI.getSize().getHeight();
		processSizesUsingTemplater(out, width, height);
	}
	
	/**
	 * Ensures the decal is the same size as it's parent (for scaled decals)
	 */
	@Override
	protected void postProcess(Element out) {	
		RectangleRenderingInformation parentRRI = (RectangleRenderingInformation) getParent().getRenderingInformation();
		double width = parentRRI.getSize().getWidth();
		double height = parentRRI.getSize().getHeight();
		Rectangle2D myBounds = ((RectangularPainter<Leaf>) this.p).bounds(theElement, this);
		double xs = width / myBounds.getWidth();
		double ys = height / myBounds.getHeight();
		
		out.setAttribute("transform", 
				"scale("+xs+","+ys+")"+
				"translate("+(-myBounds.getX())+","+(-myBounds.getY())+")"
				);
		
	}

	
}
