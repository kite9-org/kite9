package org.kite9.diagram.visualization.display.style.io;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.style.StyledDiagramElement;
import org.kite9.diagram.visualization.display.components.AbstractADLDisplayer.Justification;
import org.kite9.diagram.visualization.display.style.FixedShape;
import org.kite9.diagram.visualization.display.style.TextStyle;

/**
 * Temporary class
 * @author robmoffat
 *
 */
public class StaticStyle {
	
	public static double getSymbolWidth() {
		return 20; 
		//return ss.getSymbolSize() + ss.getInterSymbolPadding();
	}

	public static Color getWatermarkColour() {
		return new Color(0f, 0f, 0f, .2f);
	}

	public static float getLinkHopSize() {
		return 12;
	}

	public static Paint getBackground() {
		return Color.WHITE;
	}

	public static Stroke getDebugLinkStroke() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Font getDebugTextFont() {
		// TODO Auto-generated method stub
		return null;
	}

	public static double getInterSymbolPadding() {
		return 2;
	}
	
	public static java.awt.Shape createShape(SymbolShape shape, double innerSize, double x, double y) {

		switch (shape) {
		case HEXAGON:
			java.awt.Shape s1 = createRegularPolygon(x + innerSize / 2, y + innerSize
					/ 2, innerSize / 2, 6, 60);
			return s1;
		case CIRCLE:
			double cx = x + innerSize / 2;
			double cy = y + innerSize / 2;
			double r = innerSize / 2 - .5;
			java.awt.Shape s = new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r);
			return s;
		case DIAMOND:
			java.awt.Shape s3 = createRegularPolygon(x + innerSize / 2, y + innerSize
					/ 2, innerSize / 2, 4, 0);
			return s3;

		}
		
		return null;
	}
	
	protected static java.awt.Shape createRegularPolygon(double ox, double oy, double r,
			int sides, int degoff) {
		GeneralPath out = new GeneralPath();
		for (int i = 0; i <= sides; i++) {
			double angle = (Math.PI * 2 / sides) * i
					+ (((double) degoff * Math.PI * 2) / 360d);
			double curmx = Math.sin(angle);
			double xcoord = ox + r * curmx;
			double curmy = Math.cos(angle);
			double ycoord = oy + r * curmy;
			if (i == 0) {
				out.moveTo((float) xcoord, (float) ycoord);
			} else {
				out.lineTo((float) xcoord, (float) ycoord);
			}
		}

		return out;
	}
	
	public static FixedShape getSymbolShape(StyledDiagramElement h) {
		Map<String, FixedShape> out = new HashMap<String, FixedShape>();
		out.put(SymbolShape.CIRCLE.name(), new FixedShape(h, null, null, getSymbolBackgroundColor(SymbolShape.CIRCLE), createShape(SymbolShape.CIRCLE, getSymbolWidth(), 0, 0), null));
		out.put(SymbolShape.HEXAGON.name(), new FixedShape(h, null, null, getSymbolBackgroundColor(SymbolShape.HEXAGON), createShape(SymbolShape.HEXAGON, getSymbolWidth(),0, 0), null));
		out.put(SymbolShape.DIAMOND.name(), new FixedShape(h, null, null, getSymbolBackgroundColor(SymbolShape.DIAMOND), createShape(SymbolShape.DIAMOND, getSymbolWidth(), 0,0), null));
		return out;
	}

	public static Color getSymbolBackgroundColor(SymbolShape ss) {
		switch (ss) {
		case CIRCLE:
			return new Color(0, 150, 73);
		case HEXAGON:
			return new Color(207, 36, 42);
		case DIAMOND:
			return new Color(237, 106, 86);

		}

		return new Color(0, 117, 178);
	}
}
