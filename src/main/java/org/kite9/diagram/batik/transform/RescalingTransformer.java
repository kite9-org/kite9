package org.kite9.diagram.batik.transform;

import org.kite9.diagram.dom.painter.LeafPainter;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.transform.LeafTransformer;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.position.CostedDimension2D;
import org.kite9.diagram.model.position.Dimension2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

public class RescalingTransformer extends AbstractRectangularTransformer implements LeafTransformer {

	private Leaf l;
	
	public RescalingTransformer(Leaf l) {
		this.l = l;
	}
	
	DecimalFormat oneDForm = new DecimalFormat("#.0");
	
	/**
	 * Ensures the decal is the same size as it's parent (for scaled decals)
	 */
	@Override
	public Element postProcess(Painter p, Document d, XMLProcessor postProcessor) {	
		Dimension2D size = getRectangularRenderedSize(l);
		Element out = p.output(d, postProcessor);
		
		if (size == null) {
			// not a rectangular transform.
			return out;
		}
		
		double width = size.width();
		double height = size.height();
		
		if ((p instanceof LeafPainter) && (out != null)) {
			Rectangle2D myBounds = ((LeafPainter) p).bounds();
			
			if (myBounds != null) {
				double xs = width / myBounds.getWidth();
				double ys = height / myBounds.getHeight();
				
				out.setAttribute("transform", 
						"scale("+xs+","+ys+")"+
						"translate("+oneDForm.format(-myBounds.getX())+","+oneDForm.format(-myBounds.getY())+")"
						);
			}
		}
		
		return out;
	
	}

	@Override
	public Dimension2D getBounds(LeafPainter p) {
		return CostedDimension2D.Companion.getZERO();
	}

}
