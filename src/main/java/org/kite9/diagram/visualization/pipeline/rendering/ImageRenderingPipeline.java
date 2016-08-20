package org.kite9.diagram.visualization.pipeline.rendering;

import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.complete.RequiresGraphicsSourceRendererCompleteDisplayer;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;
import org.kite9.diagram.visualization.format.Renderer;
import org.kite9.diagram.xml.DiagramXMLElement;


/**
 * Given a rendering technique and a displayer, will produce image-based diagrams.
 * 
 * @author robmoffat
 *
 */
public class ImageRenderingPipeline<X> implements RenderingPipeline<X> {
	
	RequiresGraphicsSourceRendererCompleteDisplayer displayer;
	GraphicsSourceRenderer<X> renderer;

	public X render(DiagramXMLElement d) {
		return renderer.render(d);
	}
	
	public CompleteDisplayer getDisplayer() {
		return displayer;
	}

	public Renderer<X> getRenderer() {
		return renderer;
	}

	public ImageRenderingPipeline(RequiresGraphicsSourceRendererCompleteDisplayer displayer, GraphicsSourceRenderer<X> renderer) {
		super();
		this.renderer = renderer;
		this.displayer = displayer;
		this.renderer.setDisplayer(this.displayer);
	}


}
