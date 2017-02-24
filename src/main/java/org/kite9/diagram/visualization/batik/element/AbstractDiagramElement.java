package org.kite9.diagram.visualization.batik.element;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.sizing.HasLayeredGraphics;
import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.framework.common.Kite9ProcessingException;

public abstract class AbstractDiagramElement implements DiagramElement, HasLayeredGraphics {

	protected DiagramElement parent;
	protected HintMap hints;
	
	public AbstractDiagramElement(DiagramElement parent) {
		super();
		this.parent = parent;
	}

	public int compareTo(DiagramElement o) {
		return getID().compareTo(o.getID());
	}

	@Override
	public int hashCode() {
		String id = getID();
		return id.hashCode();
	}

	public AbstractDiagramElement() {
		super();
	}

	@Override
	public DiagramElement getParent() {
		return parent;
	}

	/**
	 * Remove later.
	 */
	@Override
	public Container getContainer() {
		return (Container) getParent();
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

	protected abstract GraphicsNode initMainGraphicsLayer();
	
	/**
	 * Convenience method for creating a group for this 
	 */
	public abstract IdentifiableGraphicsNode createGraphicsNode(GraphicsLayerName name);

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
	
	
}