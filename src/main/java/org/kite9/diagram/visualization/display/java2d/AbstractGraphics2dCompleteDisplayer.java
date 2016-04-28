package org.kite9.diagram.visualization.display.java2d;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.AbstractCompleteDisplayer;
import org.kite9.diagram.visualization.display.ComponentDisplayer;
import org.kite9.diagram.visualization.display.NullDisplayer;
import org.kite9.diagram.visualization.display.java2d.style.Stylesheet;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;

/**
 * This implementation allows a set of components to be displayed to a graphics 2d output.
 * @author robmoffat
 *
 */
public abstract class AbstractGraphics2dCompleteDisplayer extends AbstractCompleteDisplayer implements RequiresGraphics2DCompleteDisplayer {
	
	static final ComponentDisplayer NULL = new NullDisplayer();

	protected Dimension2D diagramSize;
		
	public AbstractGraphics2dCompleteDisplayer(Stylesheet ss, boolean buffer) {
		super(ss, buffer);
	}


	public void initialize(GraphicsSourceRenderer<?> gs, Dimension2D diagramSize) {
		this.diagramSize = diagramSize;
		displayers.clear();		
	}
}
