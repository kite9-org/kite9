package org.kite9.diagram.visualization.pipeline.full;

import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.complete.RequiresGraphicsSourceRendererCompleteDisplayer;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;
import org.kite9.diagram.visualization.pipeline.rendering.ImageRenderingPipeline;
import org.kite9.diagram.xml.DiagramXMLElement;


/**
 * Given a rendering technique and a displayer, will produce image-based diagrams.
 * 
 * @author robmoffat
 *
 */
public class ImageProcessingPipeline<X> extends AbstractArrangementPipeline implements ProcessingPipeline<X> {

	ImageRenderingPipeline<X> renderPl;
	
	public ImageProcessingPipeline(RequiresGraphicsSourceRendererCompleteDisplayer displayer, GraphicsSourceRenderer<X> renderer) {
		super();
		this.renderPl = new ImageRenderingPipeline<X>(displayer, renderer);
	}

	@Override
	public X process(DiagramXMLElement d) {
		d = arrange(d);
		return render(d);
	}

	@Override
	public X render(DiagramXMLElement d) {
		return renderPl.render(d);
	}

	@Override
	public CompleteDisplayer getDisplayer() {
		return renderPl.getDisplayer();
	}
	
}
