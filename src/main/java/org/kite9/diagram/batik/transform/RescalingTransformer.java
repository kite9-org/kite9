package org.kite9.diagram.batik.transform;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RescalingTransformer extends AbstractRectangularTransformer implements LeafTransformer  {

	private Leaf l;
	
	public RescalingTransformer(Leaf l) {
		this.l = l;
	}
	
	/**
	 * Ensures the decal is the same size as it's parent (for scaled decals)
	 */
	@Override
	public Element postProcess(Painter p, Document d) {	
		Dimension2D size = getRectangularRenderedSize(l);
		Element out = p.output(d);
		
		if (size == null) {
			// not a rectangular transform.
			return out;
		}
		
		double width = size.getWidth();
		double height = size.getHeight();
		
		if (p instanceof LeafPainter) {
			Rectangle2D myBounds = ((LeafPainter) p).bounds();
			
			if (myBounds != null) {
				double xs = width / myBounds.getWidth();
				double ys = height / myBounds.getHeight();
				
				out.setAttribute("transform", 
						"scale("+xs+","+ys+")"+
						"translate("+(-myBounds.getX())+","+(-myBounds.getY())+")"
						);
			}
		}
		
		return out;
	
	}

	@Override
	public Dimension2D getBounds(LeafPainter p) {
		return CostedDimension.ZERO;
	}

}
