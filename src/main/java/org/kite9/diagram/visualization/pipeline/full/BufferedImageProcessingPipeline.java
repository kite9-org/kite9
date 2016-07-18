package org.kite9.diagram.visualization.pipeline.full;

import java.awt.image.BufferedImage;

import org.kite9.diagram.visualization.display.complete.ADLBasicCompleteDisplayer;
import org.kite9.diagram.visualization.display.complete.GriddedCompleteDisplayer;
import org.kite9.diagram.visualization.display.complete.RequiresGraphicsSourceRendererCompleteDisplayer;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;
import org.kite9.diagram.visualization.format.png.BufferedImageRenderer;

public class BufferedImageProcessingPipeline extends ImageProcessingPipeline<BufferedImage> {

	public BufferedImageProcessingPipeline(String subtest, Class<?> theTest, boolean watermark) {
		super(new GriddedCompleteDisplayer(new ADLBasicCompleteDisplayer(watermark, false)), new BufferedImageRenderer());
	}

	public BufferedImageProcessingPipeline(RequiresGraphicsSourceRendererCompleteDisplayer displayer,	GraphicsSourceRenderer<BufferedImage> renderer) {
		super(displayer, renderer);
	}
}
