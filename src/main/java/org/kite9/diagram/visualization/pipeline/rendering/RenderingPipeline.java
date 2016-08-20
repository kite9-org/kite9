package org.kite9.diagram.visualization.pipeline.rendering;

import org.kite9.diagram.xml.DiagramXMLElement;

/**
 * Handles conversion of a sized {@link DiagramXMLElement} object into a rendered X.
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public interface RenderingPipeline<X> {

	/**
	 * Renders the diagram with the current size and position settings intact.
	 */
	public abstract X render(DiagramXMLElement d);

}