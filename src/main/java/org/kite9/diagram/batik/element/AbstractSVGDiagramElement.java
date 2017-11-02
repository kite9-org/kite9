package org.kite9.diagram.batik.element;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.HasGraphicsNode;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.batik.templater.Templater;
import org.kite9.diagram.batik.templater.ValueReplacingProcessor;
import org.kite9.diagram.batik.templater.ValueReplacingProcessor.ValueReplacer;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.xml.Kite9XMLElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents {@link DiagramElement}s that contain SVG that will need rendering.
 *  
 * @author robmoffat
 *
 */
public abstract class AbstractSVGDiagramElement extends AbstractXMLDiagramElement implements HasGraphicsNode {

	
	public AbstractSVGDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	protected GraphicsNode graphicsNodeCache;

	@Override
	public GraphicsNode getGraphicsNode() {
		GraphicsNode out = graphicsNodeCache;
		if (out == null) {
			out = initGraphicsNode();
			graphicsNodeCache = out;
			return out;
		}
		
		return out;
	}

	
	protected GraphicsNode initGraphicsNode() {
		initializeChildXMLElements();
		IdentifiableGraphicsNode out = buildGraphicsNode();
		List<GraphicsNode> nodes = initSVGGraphicsContents();
		for (GraphicsNode g : nodes) {
			out.add(g);
		}
		return out;
	}
	
	
	
	/**
	 * This implementation simply creates a group in the usual way.
	 */
	protected IdentifiableGraphicsNode buildGraphicsNode() {
		GVTBuilder builder = ctx.getGVTBuilder();
		Element e = theElement.getOwnerDocument().createElementNS(SVG12DOMImplementation.SVG_NAMESPACE_URI, "g");
		IdentifiableGraphicsNode out = (IdentifiableGraphicsNode) builder.build(ctx, e);
		out.setId(getID());
		return out;
	}
	
	/**
	 * Use this method where the DiagramElement is allowed to contain SVG contents.
	 */
	protected List<GraphicsNode> initSVGGraphicsContents() {
		List<GraphicsNode> out = new ArrayList<>();
		GVTBuilder builder = ctx.getGVTBuilder();
		NodeList childNodes = theElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child instanceof Element)  {
				GraphicsNode node = null;
				if (child instanceof Kite9XMLElement) {
					DiagramElement de = ((Kite9XMLElement) child).getDiagramElement();
					if (de instanceof HasGraphicsNode) {
						node = ((HasGraphicsNode) de).getGraphicsNode();
					}
				} else {
					// get access to the bridge, to create a graphics node.
					node = builder.build(ctx, (Element) child);
				}
				
				if (node != null) {
					out.add(node);
				}
			}
		}
		return out;
	}

	@Override
	public Rectangle2D getSVGBounds() {
		GraphicsNode gn = getGraphicsNode();
		if (gn instanceof IdentifiableGraphicsNode) {
			return ((IdentifiableGraphicsNode) gn).getSVGBounds();
		} else if (gn != null) {
			return gn.getBounds();
		} else {
			return null;
		}
	}

	protected abstract void initializeChildXMLElements();
	
	protected void processSizesUsingTemplater(Element child, RectangleRenderingInformation rri) {
		// tells the decal how big it needs to draw itself
		double [] x = new double[] {0, rri.getSize().getWidth()};
		double [] y = new double[] {0, rri.getSize().getHeight()};
		
		ValueReplacer valueReplacer = new ValueReplacer() {
			
			@Override
			public String getReplacementValue(String prefix, String attr) {
				if ("x".equals(prefix) || "y".equals(prefix)) {
					int index = Integer.parseInt(attr);
					double v = "x".equals(prefix) ? x[index] : y[index];
					return ""+v;
				} else {
					return prefix+attr;
				}
			}
		};
		
		new ValueReplacingProcessor(valueReplacer).processContents(child);
	}

	protected double padding[] = new double[4];
	protected double margin[] = new double[4];
	
	protected void initialize() {
		initializeDirectionalCssValues(padding, CSSConstants.KITE9_CSS_PADDING_PROPERTY_PREFIX);
		initializeDirectionalCssValues(margin, CSSConstants.KITE9_CSS_MARGIN_PROPERTY_PREFIX);
	}

	private void initializeDirectionalCssValues(double[] vals, String prefix) {
		vals[Direction.UP.ordinal()] = getCssDoubleValue(prefix+CSSConstants.TOP);
		vals[Direction.DOWN.ordinal()] = getCssDoubleValue(prefix+CSSConstants.BOTTOM);
		vals[Direction.LEFT.ordinal()] = getCssDoubleValue(prefix+CSSConstants.LEFT);
		vals[Direction.RIGHT.ordinal()] = getCssDoubleValue(prefix+CSSConstants.RIGHT);	
	}

	private double getCssDoubleValue(String prop) {
		Value v = getCSSStyleProperty(prop);
		return v.getFloatValue();
	}
	
	
	
}
