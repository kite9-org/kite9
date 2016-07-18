package org.kite9.diagram.visualization.display.complete;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.AbstractCompleteDisplayer;
import org.kite9.diagram.visualization.display.Displayer;
import org.kite9.diagram.visualization.display.NullDisplayer;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;

/**
 * This implementation allows a set of components to be displayed with the GraphicsSourceRenderer.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractGraphicsSourceRendererCompleteDisplayer extends AbstractCompleteDisplayer implements RequiresGraphicsSourceRendererCompleteDisplayer {
	
	static final Displayer NULL = new NullDisplayer();

	protected Dimension2D diagramSize;
	protected GraphicsSourceRenderer<?> gs;
		
	public AbstractGraphicsSourceRendererCompleteDisplayer(boolean buffer, int gridSize) {
		super(buffer, gridSize);
	}


	public void initialize(GraphicsSourceRenderer<?> gs, Dimension2D diagramSize) {
		this.diagramSize = diagramSize;
		displayers.clear();		
		this.gs = gs;
	}
}
