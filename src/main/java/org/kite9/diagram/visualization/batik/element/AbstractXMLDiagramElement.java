package org.kite9.diagram.visualization.batik.element;

import java.io.Serializable;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.sizing.HasLayeredGraphics;
import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Encapsulates an {@link StyledKite9SVGElement} as a {@link DiagramElement}.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractXMLDiagramElement extends AbstractDiagramElement implements DiagramElement, Serializable {
	
	protected StyledKite9SVGElement theElement;
	protected Kite9BridgeContext ctx;
	
	private boolean initialized = false;

	protected abstract void initialize();
	
	protected void ensureInitialized() {
		if (!initialized) {
			if (parent instanceof AbstractXMLDiagramElement) {
				((AbstractXMLDiagramElement)parent).ensureInitialized();
			}
			initialize();
			this.initialized = true;
		}
	}

	
	@Deprecated
	public StyledKite9SVGElement getTheElement() {
		return theElement;
	}

	public AbstractXMLDiagramElement(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(parent);
		this.theElement = el;
		this.ctx = ctx;
	}

	@Override
	public Value getCSSStyleProperty(String prop) {
		return theElement.getCSSStyleProperty(prop);
	}
	
	@Override
	public String getID() {
		return theElement.getID();
	}

	public String getShapeName() {
		String out = theElement.getAttribute("shape");
//		if (out.length() == 0) {
//			out = getCSSStyleProperty(CSSConstants.SHAPE_PROPERTY).getStringValue();
//		}
		
		return out;
	}

	public Diagram getDiagram() {
		if (this instanceof Diagram) {
			return (Diagram) this;
		} else {
			return ((AbstractXMLDiagramElement)getParent()).getDiagram();
		}
	}

	@Override
	public String toString() {
		String className = this.getClass().getName();
		className = className.substring(className.lastIndexOf(".")+1);
		return "["+theElement.getTagName()+":'"+getID()+"':"+className+"]";
	}

	@Override
	public HintMap getPositioningHints() {
		return null;
	}
	
	@Override
	protected GraphicsNode initMainGraphicsLayer() {
		IdentifiableGraphicsNode out = createGraphicsNode(GraphicsLayerName.MAIN);
		GVTBuilder builder = ctx.getGVTBuilder();
		for (int i = 0; i < theElement.getChildNodes().getLength(); i++) {
			Node child = theElement.getChildNodes().item(i);
			if (child instanceof Element) {
				// get access to the bridge, to create a graphics node.
				GraphicsNode node = builder.build(ctx, (Element) child);
				if (node != null) {
					out.add(node);
				}
			}
		}
		
		return out;
	}

	/**
	 * This implementation simply creates a group in the usual way.
	 */
	@Override
	public IdentifiableGraphicsNode createGraphicsNode(GraphicsLayerName name) {
		GVTBuilder builder = ctx.getGVTBuilder();
		Element e = theElement.getOwnerDocument().createElementNS(SVG12DOMImplementation.SVG_NAMESPACE_URI, "g");
		IdentifiableGraphicsNode out = (IdentifiableGraphicsNode) builder.build(ctx, e);
		out.setId(getID()+"-"+name.name());
		
		if (getParent() != null) {
			IdentifiableGraphicsNode parentNode = (IdentifiableGraphicsNode) ((HasLayeredGraphics) getParent()).getGraphicsForLayer(name);
			parentNode.add(out);
		}
		
		
		return out;
	}

	
}