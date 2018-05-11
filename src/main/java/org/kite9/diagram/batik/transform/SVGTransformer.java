package org.kite9.diagram.batik.transform;

import org.w3c.dom.Element;

/**
 * This class handles the sizing transformations added to the SVG <g> elements we
 * create.  Depending on the sizing approach, we might be transforming the content to be
 * the same size as the container, or in some way based on the container.
 * 
 * @author robmoffat
 *
 */
public interface SVGTransformer {

	/**
	 * Sets scale, position of the g element.
	 */
	public void postProcess(Element out);
	
}
