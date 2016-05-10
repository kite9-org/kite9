package org.kite9.diagram.visualization.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.primitives.DiagramElement;

/**
 * Wraps around Java's Graphics2D class.
 * 
 * @author robmoffat
 *
 */
public class BasicGraphicsLayer implements GraphicsLayer {
	
	protected final Graphics2D g2;

	public BasicGraphicsLayer(Graphics2D g2) {
		this.g2 = g2;
	}

	@Override
	public void startElement(DiagramElement de) {
	}

	@Override
	public void endElement(DiagramElement de) {
	}

	@Override
	public void setFont(Font font) {
		g2.setFont(font);
	}

	@Override
	public FontMetrics getFontMetrics(Font f) {
		return g2.getFontMetrics(f);
	}

	@Override
	public FontMetrics getFontMetrics() {
		return g2.getFontMetrics();
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		return g2.getFontRenderContext();
	}

	@Override
	public Rectangle2D getStringBounds(FontMetrics fm, String s) {
		return fm.getStringBounds(s, g2);
	}

	@Override
	public AffineTransform getTransform() {
		return g2.getTransform();
	}

	@Override
	public void setTransform(AffineTransform at) {
		g2.setTransform(at);
	}

	@Override
	public void translate(double x, double y) {
		g2.translate(x, y);
	}

	@Override
	public void scale(double x, double y) {
		g2.scale(x, y);
	}

	@Override
	public void setStroke(Stroke stroke) {
		g2.setStroke(stroke);
	}

	@Override
	public void setColor(Color color) {
		g2.setColor(color);
	}

	@Override
	public void setPaint(Paint p) {
		g2.setPaint(p);
	}

	@Override
	public Paint getPaint() {
		return g2.getPaint();
	}

	@Override
	public void fill(Shape outline) {
		g2.fill(outline);
	}

	@Override
	public void draw(Shape s) {
		g2.draw(s);
	}

	@Override
	public void dispose() {
		g2.dispose();
	}
	
	public static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(
			AffineTransform.getScaleInstance(1, 1), true, true);
	
	/**
	 * This method is required for outputting text in a platform-independent
	 * way. i.e. ignoring stuff about dpi, etc.
	 */
	public void outputText(Font font, double y, double x, String line) {
		java.awt.Shape outline;
		try {
			GlyphVector gv = font.createGlyphVector(FONT_RENDER_CONTEXT,
					line.toCharArray());
			outline = gv.getOutline((float) (x), (float) y);
			fill(outline);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Rectangle2D r2 = outline.getBounds2D();
		// debugBox(r2.getMinX(), r2.getMinY(), r2.getWidth(), r2.getHeight(),
		// Color.YELLOW, g2);
	}

}
