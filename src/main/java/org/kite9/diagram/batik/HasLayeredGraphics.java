package org.kite9.diagram.batik;

import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.model.DiagramElement;

/**
 * Means this {@link DiagramElement} can produce a {@link GraphicsNode} 
 * representing itself in a particular layer of the diagram.
 * 
 * Should only be used by Batik classes.
 * 
 * @author robmoffat
 *
 */
public interface HasLayeredGraphics extends DiagramElement {
	
	public GraphicsNode getGraphicsForLayer(Object l);
	
	public void eachLayer(Consumer<GraphicsNode> cb);
	
	/**
	 * Returns just the bounds of the SVG elements.
	 */
	public Rectangle2D getSVGBounds();
}
