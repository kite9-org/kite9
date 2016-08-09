package org.kite9.diagram.visualization.format;

import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.xml.Diagram;

/**
 * Supports rendering of a diagram in which all its components have {@link RenderingInformation} objects set.
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public interface Renderer<X> {

	/**
	 * Renders a complete diagram, marked up with position information
	 */
	public X render(Diagram d);
}
