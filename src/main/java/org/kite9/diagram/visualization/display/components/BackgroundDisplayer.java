package org.kite9.diagram.visualization.display.components;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.Stylesheet;
import org.kite9.diagram.visualization.format.GraphicsLayer;

public class BackgroundDisplayer extends AbstractDiagramDisplayer {

	public BackgroundDisplayer(CompleteDisplayer parent, Stylesheet ss, GraphicsLayer g2) {
		super(parent, ss, g2, false, 0, 0);
	}

	@Override
	public void draw(DiagramElement element, RenderingInformation ri) {
		Paint p = ss.getBackground();
		Dimension2D size = ((RectangleRenderingInformation)ri).getSize();
		paintBackground(size, p);
	}

	public void paintBackground(Dimension2D d, Paint p) {
		if (d != null) {
			g2.setPaint(p);
			if (p instanceof TexturePaint) {
				g2.fill(new Rectangle2D.Float(0f, 0f, (float) d.getWidth(), (float) d.getHeight()));
			} else {
				AffineTransform at = g2.getTransform();
				AffineTransform at2 = new AffineTransform();
				at2.scale(d.getWidth(), d.getHeight());
				g2.setTransform(at2);
				g2.fill(new Rectangle(0, 0, 1, 1));
				g2.setTransform(at);
			}
		}
	}


}
