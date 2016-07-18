package org.kite9.diagram.visualization.display.components;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.visualization.display.style.FixedShape;
import org.kite9.diagram.visualization.display.style.io.StaticStyle;
import org.kite9.diagram.visualization.format.GraphicsLayer;

public class SymbolDisplayer extends AbstractADLDisplayer {


	protected double getSymbolWidth() {
		return StaticStyle.getSymbolWidth() + StaticStyle.getInterSymbolPadding();
	}


	protected double getSymbolBaseline(Font textFont, Font f, GraphicsLayer g2) {
//		//double desc = 0;
//		if (textFont != null) {
//			FontMetrics textSize = g2.getFontMetrics(textFont);
//			//desc = textSize.getMaxDescent();
//		}

		return ss.getSymbolSize();
	}

	protected CostedDimension drawSymbol(String sym, GraphicsLayer g2, double ox,
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
		g2.outputText(symbolFont, ypos, xpos, sym);
		// g2.setColor(Color.RED);
		// Shape s = new Rectangle2D.Double(xpos, ypos - textSize.getHeight(),
		// textSize.getWidth(), textSize.getHeight());
		// g2.draw(s);
		return new CostedDimension(outerSize, innerSize, 0);
	}

}
