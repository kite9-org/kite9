package org.kite9.diagram.visualization.display.components;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.complete.TransformedPaint;
import org.kite9.diagram.visualization.display.style.io.StaticStyle;
import org.kite9.diagram.visualization.format.GraphicsLayer;

public class BackgroundDisplayer extends AbstractDiagramDisplayer {

	public BackgroundDisplayer(CompleteDisplayer parent, GraphicsLayer g2) {
		super(parent, g2, false);
	}

	@Override
	public void draw(DiagramElement element, RenderingInformation ri) {
		Paint p = StaticStyle.getBackground();
		Dimension2D size = ((RectangleRenderingInformation)ri).getSize();
		paintBackground(size, p);
	}

	public void paintBackground(Dimension2D d, Paint p) {
		if (d != null) {
			if ((p instanceof TexturePaint) || (p instanceof Color))  {
				g2.setPaint(p);
				g2.fill(new Rectangle2D.Float(0f, 0f, (float) d.getWidth(), (float) d.getHeight()));
			} else {
				AffineTransform at = g2.getTransform();
				AffineTransform at2 = new AffineTransform();
				at2.scale(d.getWidth(), d.getHeight());
				g2.setTransform(at2);
				TransformedPaint tp = new TransformedPaint(p, at2, "background");
				g2.setPaint(tp);
				g2.fill(new Rectangle(0, 0, 1, 1));
				g2.setTransform(at);
			}
		}
	}


}
