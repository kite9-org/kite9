package org.kite9.diagram.performance;

import java.awt.image.BufferedImage;

import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.display.complete.RequiresGraphicsSourceRendererCompleteDisplayer;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.pipeline.BufferedImageProcessingPipeline;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.framework.xml.DiagramXMLElement;

public class TimingPipeline extends BufferedImageProcessingPipeline {
	
	public TimingPipeline(RequiresGraphicsSourceRendererCompleteDisplayer displayer,
			GraphicsSourceRenderer<BufferedImage> renderer) {
		super(displayer, renderer);
	}

	Metrics m = new Metrics(this.getClass().getName());
	
	@Override
	public Compaction compactOrthogonalization(Orthogonalization o) {
		long start = System.currentTimeMillis();
		Compaction out = super.compactOrthogonalization(o);
		m.compactionTime = System.currentTimeMillis() - start;
		return out;
	}

	@Override
	public Orthogonalization createOrthogonalization(Planarization p) {
		long start = System.currentTimeMillis();
		Orthogonalization out =   super.createOrthogonalization(p);
		m.orthogonalizationTime = System.currentTimeMillis() - start;
		return out;
	}

	@Override
	public Planarization createPlanarization(DiagramXMLElement d) {
		long start = System.currentTimeMillis();
		Planarization out =  super.createPlanarization(d);
		m.planarizationTime = System.currentTimeMillis() - start;
		return out;
	}
}
