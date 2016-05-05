package org.kite9.diagram.visualization.display.complete;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.ComponentDisplayer;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;

/**
 * Used for {@link ComponentDisplayer}s that require graphics2d.
 * @author robmoffat
 *
 */
public interface RequiresGraphicsSourceRendererCompleteDisplayer extends CompleteDisplayer {

	public void initialize(GraphicsSourceRenderer<?> gs, Dimension2D diagramSize);
	
}