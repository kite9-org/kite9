package org.kite9.diagram.visualization.display.java2d;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.ComponentDisplayer;
import org.kite9.diagram.visualization.display.java2d.style.io.PathConverter;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;

/**
 * Used for {@link ComponentDisplayer}s that require graphics2d.
 * @author robmoffat
 *
 */
public interface RequiresGraphics2DCompleteDisplayer extends CompleteDisplayer {

	public void initialize(GraphicsSourceRenderer<?> gs, Dimension2D diagramSize);
	
	public void setPathConverter(PathConverter pc);
	
	public PathConverter getPathConverter();

}