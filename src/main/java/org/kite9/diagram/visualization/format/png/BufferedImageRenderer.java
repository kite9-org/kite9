package org.kite9.diagram.visualization.format.png;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.format.AbstractScalingGraphicsSourceRenderer;
import org.kite9.diagram.visualization.format.BasicGraphicsLayer;
import org.kite9.diagram.visualization.format.GraphicsLayer;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.xml.DiagramXMLElement;

public class BufferedImageRenderer extends AbstractScalingGraphicsSourceRenderer<BufferedImage> {

	public BufferedImageRenderer(Integer width, Integer height) {
		super(width, height);
	}
	
	public BufferedImageRenderer() {
		this(null, null);
	}

	BufferedImage bi;
	Graphics2D g2;

	public BufferedImage render(DiagramXMLElement something) {
		Diagram de = something.getDiagramElement();
		Dimension2D out = size(de);
		dea.initialize(this, out);
		drawDiagramElements(de);
		dea.finish();
		bi.flush();
		g2.dispose();
		return bi;
	}

	public GraphicsLayer getGraphics(GraphicsLayerName layer, float scale, Dimension2D imageSize, Dimension2D diagramSize) {
		
		if ((bi==null) || (bi.getWidth() != imageSize.getWidth()) ||  (bi.getHeight() != imageSize.getHeight())) {
			bi = new BufferedImage((int) imageSize.getWidth(), (int) imageSize.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			g2 = bi.createGraphics();	
			setRenderingHints(g2);
			applyScaleAndTranslate(g2, scale, imageSize, diagramSize);
		}
		
		return new BasicGraphicsLayer(g2);
	}
}
