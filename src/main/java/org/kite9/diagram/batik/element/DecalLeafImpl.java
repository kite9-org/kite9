package org.kite9.diagram.batik.element;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.RectangularPainter;
import org.kite9.diagram.batik.templater.ValueReplacingProcessor;
import org.kite9.diagram.batik.templater.ValueReplacingProcessor.ValueReplacer;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

public class DecalLeafImpl extends AbstractRectangularDiagramElement implements Decal, Leaf {

	public DecalLeafImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, RectangularPainter<Leaf> lo) {
		super(el, parent, ctx, lo);
	}
	
	@Override
	protected void initSizing() {
		super.initSizing();
		if (this.sizing != DiagramElementSizing.ADAPTIVE) {
			this.sizing = DiagramElementSizing.SCALED;		 
		}
	}

	@Override
	public DiagramElementSizing getSizing() {
		ensureInitialized();
		return sizing;
	}

	@Override
	public Rectangle2D getSVGBounds() {
		ensureInitialized();
		return ((RectangularPainter<?>) this.p).bounds(theElement);
	}

	@Override
	protected Element postProcess(Element out) {
		RectangleRenderingInformation parentRRI = (RectangleRenderingInformation) getParent().getRenderingInformation();
		double width = parentRRI.getSize().getWidth();
		double height = parentRRI.getSize().getHeight();
		
		if (this.sizing == DiagramElementSizing.SCALED) {
			Rectangle2D myBounds = getSVGBounds();
			double xs = width / myBounds.getWidth();
			double ys = height / myBounds.getHeight();
			
			out.setAttribute("transform", 
					"scale("+xs+","+ys+")"+
					"translate("+(-myBounds.getX())+","+(-myBounds.getY())+")"
					);
			
		} else {
			processSizesUsingTemplater(out, width, height); 
		}
		return out;
	}

	protected void processSizesUsingTemplater(Element child, double width, double height) {
		// tells the decal how big it needs to draw itself
		double [] x = new double[] {0, width};
		double [] y = new double[] {0, height};
		
		ValueReplacer valueReplacer = new ValueReplacer() {
			
			@Override
			public String getReplacementValue(String in) {
				try {
					if (in.startsWith("x") || in.startsWith("y")) {
						int index = Integer.parseInt(in.substring(1));
						double v = ('x' == in.charAt(0)) ? x[index] : y[index];
						return ""+v;
					}
				} catch (NumberFormatException e) {
					// just move on...
				} 
				
				return in;
			}
		};
		
		new ValueReplacingProcessor(valueReplacer).processContents(child);
	}
}
