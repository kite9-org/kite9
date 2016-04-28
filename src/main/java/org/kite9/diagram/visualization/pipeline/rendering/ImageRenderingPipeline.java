package org.kite9.diagram.visualization.pipeline.rendering;

import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.RequiresGraphics2DCompleteDisplayer;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;
import org.kite9.diagram.visualization.format.Renderer;


/**
 * Given a rendering technique and a displayer, will produce image-based diagrams.
 * 
 * @author robmoffat
 *
 */
public class ImageRenderingPipeline<X> implements RenderingPipeline<X> {
	
	RequiresGraphics2DCompleteDisplayer displayer;
	GraphicsSourceRenderer<X> renderer;

	public X render(Diagram d) {
		return renderer.render(d);
	}
	
	public CompleteDisplayer getDisplayer() {
		return displayer;
	}

	public Renderer<X> getRenderer() {
		return renderer;
	}

	public ImageRenderingPipeline(RequiresGraphics2DCompleteDisplayer displayer, GraphicsSourceRenderer<X> renderer) {
		super();
		this.renderer = renderer;
		this.displayer = displayer;
		this.renderer.setDisplayer(this.displayer);
	}


}
