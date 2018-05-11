package org.kite9.framework.dom;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.ContentTransform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Means this {@link DiagramElement} can output SVG
 * representing itself in a particular layer of the diagram.
 * 
 * Should only be used by Batik classes.
 * 
 * @author robmoffat
 *
 */
public interface HasSVGGraphics extends DiagramElement {
	
	/**
	 * Creates SVG elements representing this DiagramElement and anything 
	 * nested within it.
	 */
	public Element output(Document d);
	
	/**
	 * Returns details of how the svg is transformed as it's written.
	 */
	public ContentTransform getTransform();

}
