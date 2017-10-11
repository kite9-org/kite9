package org.kite9.diagram.batik.element;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.HasLayeredGraphics;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.element.Templater.ValueReplacer;
import org.kite9.diagram.batik.layers.GraphicsLayerName;
import org.kite9.diagram.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.BoxShadow;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

/**
 * Represents {@link DiagramElement}s that contain SVG that will need rendering.
 *  
 * @author robmoffat
 *
 */
public abstract class AbstractSVGDiagramElement extends AbstractXMLDiagramElement implements HasLayeredGraphics {

	
	public AbstractSVGDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}


	/**
	 * Handles Batik bits
	 */
	protected Map<Object, GraphicsNode> graphicsNodeCache = new HashMap<>();

	@Override
	public GraphicsNode getGraphicsForLayer(Object l) {
		if (l instanceof GraphicsLayerName) {
			GraphicsLayerName name = (GraphicsLayerName) l;
			GraphicsNode out = graphicsNodeCache.get(name);
			if (out == null) {
				out = initGraphicsForLayer(name);
				graphicsNodeCache.put(name, out);
				return out;
			}
			
			return out;
		} else {
			throw new Kite9ProcessingException("Unrecognised Layer: "+l);
		}
	}

	/**
	 * Override this unless you only need to implement the {@link GraphicsLayerName}.MAIN layer.
	 */
	protected GraphicsNode initGraphicsForLayer(GraphicsLayerName name) {
		initializeChildXMLElements();
		return name.createLayer(getID(), ctx, theElement, this);
	}

	@Override
	public void eachLayer(Consumer<GraphicsNode> cb) {
		for (GraphicsLayerName name : GraphicsLayerName.values()) {
			GraphicsNode layerNode = getGraphicsForLayer(name);
			if (layerNode != null) {
				cb.accept(layerNode);
			}
		}
	}

	@Override
	public Rectangle2D getSVGBounds() {
		GraphicsNode gn = getGraphicsForLayer(GraphicsLayerName.MAIN);
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
		
		ctx.getTemplater().performReplace(child, new ValueReplacer() {
			
			@Override
			public String getText() {
				return null;
			}
			
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
		});
	}


	/**
	 * This implementation simply creates a group in the usual way.
	 */
	public IdentifiableGraphicsNode createGraphicsNode(GraphicsLayerName name) {
		GVTBuilder builder = ctx.getGVTBuilder();
		Element e = theElement.getOwnerDocument().createElementNS(SVG12DOMImplementation.SVG_NAMESPACE_URI, "g");
		IdentifiableGraphicsNode out = (IdentifiableGraphicsNode) builder.build(ctx, e);
		out.setId(getID()+"-"+name.name());
		out.setLayer(name);		
		return out;
	}

	protected double padding[] = new double[4];
	protected double margin[] = new double[4];
	protected BoxShadow boxShadow;
	
	protected void initialize() {
		initializeDirectionalCssValues(padding, "padding");
		initializeDirectionalCssValues(margin, "margin");
		initializeBoxShadow();
	}

	private void initializeBoxShadow() {
		this.boxShadow = BoxShadow.constructBoxShadow(getTheElement());
	}

	private void initializeDirectionalCssValues(double[] vals, String prefix) {
		vals[Direction.UP.ordinal()] = getCssDoubleValue(prefix+"-top");
		vals[Direction.DOWN.ordinal()] = getCssDoubleValue(prefix+"-bottom");
		vals[Direction.LEFT.ordinal()] = getCssDoubleValue(prefix+"-left");
		vals[Direction.RIGHT.ordinal()] = getCssDoubleValue(prefix+"-right");	
	}

	private double getCssDoubleValue(String prop) {
		Value v = getCSSStyleProperty(prop);
		return v.getFloatValue();
	}

	@Override
	public BoxShadow getShadow() {
		ensureInitialized();
		return boxShadow;
	}
	
	
	
}
