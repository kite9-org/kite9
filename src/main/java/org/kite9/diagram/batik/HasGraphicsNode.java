package org.kite9.diagram.batik;

import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.templater.Templater;
import org.kite9.diagram.model.DiagramElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Means this {@link DiagramElement} can produce a {@link GraphicsNode} 
 * representing itself in a particular layer of the diagram.
 * 
 * Should only be used by Batik classes.
 * 
 * @author robmoffat
 *
 */
public interface HasGraphicsNode extends DiagramElement {
	
	/**
	 * Returns the Batik graphics node, which can be used for measurement, 
	 * or outputting. 
	 */
	public GraphicsNode getGraphicsNode();
		
	/**
	 * Returns just the bounds of the SVG elements.
	 */
	public Rectangle2D getSVGBounds();
	
	/**
	 * Creates SVG elements representing this DiagramElement and anything 
	 * nested within it.
	 */
	public Element output(Document d, Templater t);
}
