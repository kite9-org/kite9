package org.kite9.diagram.batik.element;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.GraphicsLayerName;
import org.kite9.diagram.batik.HasLayeredGraphics;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.xml.Kite9XMLElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
		if (name == GraphicsLayerName.MAIN) {
			return initMainGraphicsLayer();
		} else {
			return null;
		}
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
		} else if (gn instanceof GraphicsNode) {
			return gn.getBounds();
		} else {
			return null;
		}
	}
	
	protected IdentifiableGraphicsNode initMainGraphicsLayer() {
		IdentifiableGraphicsNode out = createGraphicsNode(GraphicsLayerName.MAIN);
		initSVGGraphicsContents(out);
		return out;
	}

	/**
	 * Use this method where the DiagramElement is allowed to contain SVG contents.
	 */
	protected void initSVGGraphicsContents(IdentifiableGraphicsNode out) {
		GVTBuilder builder = ctx.getGVTBuilder();
		NodeList childNodes = theElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child instanceof Element) {
				// get access to the bridge, to create a graphics node.
				GraphicsNode node = builder.build(ctx, (Element) child);
				if (node != null) {
					out.add(node);
				}
			}
		}
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
	
	protected void initialize() {
		initializeDirectionalCssValues(padding, "padding");
		initializeDirectionalCssValues(margin, "margin");
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
	
	
	
}
