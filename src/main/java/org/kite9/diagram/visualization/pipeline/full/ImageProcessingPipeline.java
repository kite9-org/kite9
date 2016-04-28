package org.kite9.diagram.visualization.pipeline.full;

import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.RequiresGraphics2DCompleteDisplayer;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;
import org.kite9.diagram.visualization.pipeline.rendering.ImageRenderingPipeline;


/**
 * Given a rendering technique and a displayer, will produce image-based diagrams.
 * 
 * @author robmoffat
 *
 */
public class ImageProcessingPipeline<X> extends AbstractArrangementPipeline implements ProcessingPipeline<X> {

	ImageRenderingPipeline<X> renderPl;
	
	public ImageProcessingPipeline(RequiresGraphics2DCompleteDisplayer displayer, GraphicsSourceRenderer<X> renderer) {
		super();
		this.renderPl = new ImageRenderingPipeline<X>(displayer, renderer);
	}

	@Override
	public X process(Diagram d) {
		d = arrange(d);
		return render(d);
	}

	@Override
	public X render(Diagram d) {
		return renderPl.render(d);
	}

	@Override
	public CompleteDisplayer getDisplayer() {
		return renderPl.getDisplayer();
	}
	
}
