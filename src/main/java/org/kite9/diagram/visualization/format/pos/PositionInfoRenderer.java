package org.kite9.diagram.visualization.format.pos;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.complete.RequiresGraphicsSourceRendererCompleteDisplayer;
import org.kite9.diagram.visualization.format.AbstractGraphicsSourceRenderer;
import org.kite9.diagram.visualization.format.BasicGraphicsLayer;
import org.kite9.diagram.visualization.format.GraphicsLayer;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.xml.DiagramXMLElement;

/**
 * Position info renderer doesn't render an image, it returns the diagram containing information about each graphical
 * item in the image, and where it is. This is so we can send positional information over the wire to be rendered by
 * something else.
 * 
 * @author robmoffat
 * 
 */
public class PositionInfoRenderer extends AbstractGraphicsSourceRenderer<DiagramXMLElement> {

	BufferedImage bi;
	Graphics2D g2;

	public GraphicsLayer getGraphicsLayer(GraphicsLayerName layer, Dimension2D size) {
		if (bi == null) {
			bi = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
			g2 = bi.createGraphics();
			setRenderingHints(g2);
		}

		return new BasicGraphicsLayer(g2);
	}

	@Override
	public void setDisplayer(RequiresGraphicsSourceRendererCompleteDisplayer dea) {
		super.setDisplayer(dea);
		dea.setOutputting(false);
	}

	@Override
	public DiagramXMLElement render(DiagramXMLElement d) {
		Diagram de = d.getDiagramElement();
		DiagramChecker.checkConnnectionElements(d, DiagramChecker.SET_CONTRADICTING);
		Dimension2D out = size(de);
		dea.initialize(this, out);
		drawDiagramElements(de);
		dea.finish();
		bi.flush();
		g2.dispose();
		return d;
	}

}
