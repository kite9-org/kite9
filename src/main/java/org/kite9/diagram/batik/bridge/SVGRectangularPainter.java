package org.kite9.diagram.batik.bridge;

import java.awt.geom.Rectangle2D;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.templater.Kite9ExpandingCopier;
import org.kite9.diagram.batik.templater.ParentElementValueReplacer;
import org.kite9.diagram.batik.templater.ValueReplacingProcessor;
import org.kite9.diagram.batik.templater.ValueReplacingProcessor.ValueReplacer;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles painting for {@link DiagramElementType.SVG}
 * @author robmoffat
 *
 */
public class SVGRectangularPainter implements RectangularPainter<Leaf> {

	private Kite9BridgeContext ctx;
	
	public SVGRectangularPainter(Kite9BridgeContext ctx) {
		super();
		this.ctx = ctx;
	}

	/**
	 * The basic output approach is to turn any DiagramElement into a <g> tag, with the same ID set
	 * as the DiagramElement.  
	 */
	@Override
	public Element output(Document d, StyledKite9SVGElement theElement, Leaf r) {
		Element out = d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12OMDocument.SVG_G_TAG);
		new Kite9ExpandingCopier("", out).processContents(theElement);
		out.setAttribute(SVG12OMDocument.SVG_ID_ATTRIBUTE, r.getID());
		out.setAttribute("class", theElement.getCSSClass());
		out.setAttribute("style", theElement.getAttribute("style"));
		return out;
	}

	@Override
	public Rectangle2D bounds(Element theElement) {
		GraphicsNode gn = getGraphicsNode(theElement);
		return gn.getBounds();
	}


	private GraphicsNode graphicsNodeCache;

	private GraphicsNode getGraphicsNode(Element theElement) {
		GraphicsNode out = graphicsNodeCache;
		if (out == null) {
			out = initGraphicsNode(theElement);
			graphicsNodeCache = out;
			return out;
		}
		
		return out;
	}

	
	protected GraphicsNode initGraphicsNode(Element theElement) {
		// do some kind of init on the theElement, then...
		
		CompositeGraphicsNode out = buildGraphicsNode(theElement);
//		List<GraphicsNode> nodes = initSVGGraphicsContents();
//		for (GraphicsNode g : nodes) {
//			out.add(g);
//		}
		return out;
	}

//	/**
//	 * Use this method where the DiagramElement is allowed to contain SVG contents.
//	 */
//	private List<GraphicsNode> initSVGGraphicsContents() {
//		List<GraphicsNode> out = new ArrayList<>();
//		GVTBuilder builder = ctx.getGVTBuilder();
//		NodeList childNodes = theElement.getChildNodes();
//		for (int i = 0; i < childNodes.getLength(); i++) {
//			Node child = childNodes.item(i);
//			if (child instanceof Element)  {
//				GraphicsNode node = null;
//				if (child instanceof Kite9XMLElement) {
//					DiagramElement de = ((Kite9XMLElement) child).getDiagramElement();
//					if (de instanceof AbstractBatikDiagramElement) {
//						node = ((AbstractBatikDiagramElement) de).getGraphicsNode();
//					}
//				} else {
//					// get access to the bridge, to create a graphics node.
//					node = builder.build(ctx, (Element) child);
//				}
//				
//				if (node != null) {
//					out.add(node);
//				}
//			}
//		}
//		return out;
//	}

	
	protected void processSizesUsingTemplater(Element child, RectangleRenderingInformation rri) {
		// tells the decal how big it needs to draw itself
		double [] x = new double[] {0, rri.getSize().getWidth()};
		double [] y = new double[] {0, rri.getSize().getHeight()};
		
		ValueReplacer valueReplacer = new ParentElementValueReplacer((Element) child.getParentNode()) {
			
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

				
				if (in.equals("path")) {
					
				}
				
				return super.getReplacementValue(in);
			}
		};
		
		new ValueReplacingProcessor(valueReplacer).processContents(child);
	}
	
	
	/**
	 * This implementation simply creates a group in the usual way.
	 */
	protected CompositeGraphicsNode buildGraphicsNode(Element theElement) {
		GVTBuilder builder = ctx.getGVTBuilder();
		CompositeGraphicsNode out = (CompositeGraphicsNode) builder.build(ctx, theElement);
		return out;
	}
	
	
	
}
