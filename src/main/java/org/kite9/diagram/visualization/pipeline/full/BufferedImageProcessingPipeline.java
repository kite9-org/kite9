package org.kite9.diagram.visualization.pipeline.full;

import java.awt.image.BufferedImage;

import org.kite9.diagram.visualization.display.java2d.GriddedCompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.RequiresGraphics2DCompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.adl_basic.ADLBasicCompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.style.Stylesheet;
import org.kite9.diagram.visualization.display.java2d.style.sheets.BasicStylesheet;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;
import org.kite9.diagram.visualization.format.png.BufferedImageRenderer;

public class BufferedImageProcessingPipeline extends ImageProcessingPipeline<BufferedImage> {

	public BufferedImageProcessingPipeline(String subtest, Class<?> theTest, boolean watermark) {
		this(new BasicStylesheet(), subtest, theTest, watermark);
	}

	public BufferedImageProcessingPipeline(Stylesheet ss, String subtest, Class<?> theTest, boolean watermark) {
		super(new GriddedCompleteDisplayer(new ADLBasicCompleteDisplayer(ss, watermark, false), ss), new BufferedImageRenderer());
	}

	public BufferedImageProcessingPipeline(RequiresGraphics2DCompleteDisplayer displayer,	GraphicsSourceRenderer<BufferedImage> renderer) {
		super(displayer, renderer);
	}
}
