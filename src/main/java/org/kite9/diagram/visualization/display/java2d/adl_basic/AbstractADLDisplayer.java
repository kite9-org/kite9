package org.kite9.diagram.visualization.display.java2d.adl_basic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.ComponentDisplayer;
import org.kite9.diagram.visualization.display.java2d.style.FixedShape;
import org.kite9.diagram.visualization.display.java2d.style.Stylesheet;
import org.kite9.diagram.visualization.display.java2d.style.io.PathConverter;

/**
 * Provides useful arranger functionality for attr general to drawing ADL
 * diagrams.
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractADLDisplayer implements ComponentDisplayer {

	public static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(
			AffineTransform.getScaleInstance(1, 1), true, true);

	public enum Justification {
		LEFT, RIGHT, CENTER;

		public double getIndent(double margin) {
			if (this == LEFT) {
				return 0;
			} else if (this == RIGHT) {
				return margin;
			} else {
				return margin / 2.0;
			}
		}

	};

	protected Graphics2D g2;

	protected Stylesheet ss;

	protected boolean shadow;

	public CostedDimension size(DiagramElement element, Dimension2D within) {
		throw new UnsupportedOperationException(
				"Size is not implemented for this component" + element);
	}

	int xo, yo;
	protected CompleteDisplayer parent;
	
	public AbstractADLDisplayer(Graphics2D g2, Stylesheet ss) {
		this.g2 = g2;
		this.ss = ss;
	}
	
	public PathConverter getPathConverter() {
		return ((ADLBasicCompleteDisplayer)parent).getPathConverter();
	}

	public AbstractADLDisplayer(CompleteDisplayer parent, Graphics2D g2, Stylesheet ss, boolean shadow,
			int xo, int yo) {
		this.g2 = g2;
		this.shadow = shadow;
		this.xo = xo;
		this.yo = yo;
		this.ss = ss;
		this.parent = parent;
	}

	public CostedDimension arrangeString(Font font, Color color, String str,
			Dimension2D topLeft, Dimension2D area, boolean draw,
			Justification justification, double baseline) {
		return arrangeString(font, color, str, topLeft, area,
				CostedDimension.ZERO, CostedDimension.ZERO, draw,
				justification, baseline);
	}

	public CostedDimension arrangeString(Font font, Color color, String str,
			Dimension2D topLeft, Dimension2D area, Dimension2D tlpadding,
			Dimension2D brpadding, boolean draw, Justification justification,
			double baseline) {

		if ((str == null) || (str.trim().length() == 0))
			return new CostedDimension(0, 0, 0);

		g2.setFont(font);
		FontMetrics metrics = null;
		try {
			metrics = g2.getFontMetrics();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Dimension2D within = new Dimension2D(area.getWidth() - tlpadding.getWidth()
				- brpadding.getWidth(), area.getHeight() - tlpadding.getHeight()
				- brpadding.getHeight());
		CostedDimension textArea = sizeText(str, metrics, within);

		CostedDimension out = new CostedDimension(textArea.getWidth()
				+ tlpadding.getWidth() + brpadding.getWidth(), textArea.getHeight()
				+ tlpadding.getHeight() + brpadding.getHeight(), textArea.cost);

		if (draw) {
			String[] lines = str.split("\n");
			double y = topLeft.y() + tlpadding.y() + baseline
					- ((lines(str) - 1) * metrics.getHeight());
			double x = topLeft.x() + tlpadding.x();
			// debugBox((int)(x), (int)(y - metrics.getAscent()), (int)(out.x()
			// - brpadding.x()), (int)(out.y() - brpadding.y()), Color.GREEN,
			// g2);
			for (String line : lines) {
				double usedWidth = metrics.stringWidth(line.trim());
				double margin = within.getWidth() - usedWidth;
				double indent = justification.getIndent(margin);
				g2.setColor(color);
				outputText(font, y, x + indent, line.trim(), g2);

				y += metrics.getHeight();
			}
		}

		return out;
	}

	/**
	 * This method is required for outputting text in a platform-independent
	 * way. i.e. ignoring stuff about dpi, etc.
	 */
	public static void outputText(Font font, double y, double x, String line, Graphics2D g2) {
		java.awt.Shape outline;
		try {
			GlyphVector gv = font.createGlyphVector(FONT_RENDER_CONTEXT,
					line.toCharArray());
			outline = gv.getOutline((float) (x), (float) y);
			g2.fill(outline);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Rectangle2D r2 = outline.getBounds2D();
		// debugBox(r2.getMinX(), r2.getMinY(), r2.getWidth(), r2.getHeight(),
		// Color.YELLOW, g2);
	}

	public CostedDimension sizeText(String str, FontMetrics metrics,
			Dimension2D within) {
		String[] lines = str.split("\n");
		int maxLength = 0;
		for (String string : lines) {
			maxLength = Math.max(metrics.stringWidth(string), maxLength);
		}

		int height = lines.length * (metrics.getHeight());

		return new CostedDimension(maxLength, height, within);
	}

	protected CostedDimension arrangeVertically(Dimension2D... textSize) {
		double maxWidth = 0;
		double height = 0;
		int cost = 0;
		for (Dimension2D dimension : textSize) {
			maxWidth = Math.max(dimension.x(), maxWidth);
			height += dimension.y();
			cost += dimension instanceof CostedDimension ? ((CostedDimension) dimension).cost
					: 0;
		}
		CostedDimension cd = new CostedDimension(maxWidth, height, cost);
		return cd;
	}

	protected CostedDimension arrangeHorizontally(Dimension2D... textSize) {
		double width = 0;
		double height = 0;
		int cost = 0;
		for (Dimension2D dimension : textSize) {
			width += dimension.x();
			height = Math.max(dimension.y(), height);
			cost += dimension instanceof CostedDimension ? ((CostedDimension) dimension).cost
					: 0;
		}
		return new CostedDimension(width, height, cost);
	}

	public boolean isVisibleElement(DiagramElement element) {
		return true;
	}

	protected double getSymbolWidth() {
		return ss.getSymbolSize() + ss.getInterSymbolPadding();
	}

	protected static void debugBox(double x, double y, double width,
			double height, Color k, Graphics2D g2) {
		Stroke old = g2.getStroke();
		Color c = g2.getColor();
		g2.setStroke(new BasicStroke(1));
		g2.setColor(k);
		g2.drawRect((int) x, (int) y, (int) width, (int) height);
		g2.setColor(c);
		g2.setStroke(old);
	}

	protected double getBaseline(Font f, Graphics2D g2, String theText) {
		if (f == null) {
			return 0;
		}
		int lc = lines(theText) - 1;
		FontMetrics fm = g2.getFontMetrics(f);
		return fm.getMaxAscent() + (lc * fm.getHeight());
	}

	public int lines(String someText) {
		return someText == null ? 1 : someText.split("\n").length;
	}

	protected double getTotalHeight(FontMetrics fm) {
		return fm.getMaxAscent() + fm.getMaxDescent();
	}

	protected double getLineHeight(FontMetrics fm) {
		return fm.getHeight();
	}

	protected double getSymbolBaseline(Font textFont, Font f, Graphics2D g2) {
//		//double desc = 0;
//		if (textFont != null) {
//			FontMetrics textSize = g2.getFontMetrics(textFont);
//			//desc = textSize.getMaxDescent();
//		}

		return ss.getSymbolSize();
	}

	protected CostedDimension drawSymbol(String sym, Graphics2D g2, double ox,
			double oy, FixedShape shape, Font symbolFont,
			double baseline) {
		g2.setFont(symbolFont);
		FontMetrics symFontMetrics = g2.getFontMetrics(symbolFont);
		GlyphVector gv = symbolFont.createGlyphVector(
				g2.getFontRenderContext(), sym);
		Rectangle2D textSize = gv.getLogicalBounds();

		double outerSize = ss.getSymbolSize() + ss.getInterSymbolPadding() * 2;
		double innerSize = ss.getSymbolSize();
		double symbolBaseline = symFontMetrics.getMaxAscent();
		double symbolDesc = symFontMetrics.getMaxDescent();
		double y = oy + baseline - innerSize;
		double x = ox + ss.getInterSymbolPadding();

		// draw the shape
		Shape path = shape.getPath();
		g2.setPaint(shape.getBackground(path));
		AffineTransform orig = g2.getTransform();
		AffineTransform at = new AffineTransform();
		at.translate(orig.getTranslateX(), orig.getTranslateY());
		at.scale(orig.getScaleX(), orig.getScaleY());
		at.translate(x, y);
		g2.setTransform(at);
		g2.fill(shape.getPath());
		g2.setTransform(orig);

		
		
		
		double internalPadding = innerSize - symbolBaseline - symbolDesc;
		

		float xpos = (float) (x + (innerSize / 2d) - (textSize.getWidth() / 2d));
		float ypos = (float) (y + (internalPadding / 2d) + symbolBaseline);
//		g2.setColor(Color.RED);
//		g2.drawRect((int) x, (int) (y), (int) ss.getSymbolSize(), (int) ss.getSymbolSize());

		g2.setColor(ss.getSymbolTextStyle().getColor());
		outputText(symbolFont, ypos, xpos, sym, g2);
		// g2.setColor(Color.RED);
		// Shape s = new Rectangle2D.Double(xpos, ypos - textSize.getHeight(),
		// textSize.getWidth(), textSize.getHeight());
		// g2.draw(s);
		return new CostedDimension(outerSize, innerSize, 0);
	}

	private boolean outputting = true;

	public boolean isOutputting() {
		return outputting;
	}

	public void setOutputting(boolean outputting) {
		this.outputting = outputting;
	}

	@Override
	public boolean requiresDimension(DiagramElement de) {
		return true;
	}
	
	
	
}
