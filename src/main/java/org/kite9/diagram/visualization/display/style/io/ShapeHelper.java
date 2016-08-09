package org.kite9.diagram.visualization.display.style.io;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.batik.ext.awt.geom.Polygon2D;
import org.kite9.diagram.visualization.display.style.ConnectionTemplate;
import org.kite9.diagram.visualization.display.style.FlexibleShape;
import org.kite9.diagram.visualization.display.style.shapes.CircleFlexibleShape;
import org.kite9.diagram.visualization.display.style.shapes.DiamondFlexibleShape;
import org.kite9.diagram.visualization.display.style.shapes.DividerFlexibleShape;
import org.kite9.diagram.visualization.display.style.shapes.EllipseFlexibleShape;
import org.kite9.diagram.visualization.display.style.shapes.FlowchartShapes;
import org.kite9.diagram.visualization.display.style.shapes.HexagonFlexibleShape;
import org.kite9.diagram.visualization.display.style.shapes.RoundedRectFlexibleShape;
import org.kite9.diagram.visualization.display.style.shapes.UMLShapes;
import org.kite9.diagram.xml.Symbol.SymbolShape;
import org.kite9.framework.common.Kite9ProcessingException;

/**
 * Provides logic to create the ADL symbol shapes and arrow ends.
 * 
 * NB:  Stylesheets are no longer thread-safe as they employ SVGHelper.
 * 
 * @author robmoffat
 * 
 */
public abstract class ShapeHelper {
	
	public static java.awt.Shape createRegularPolygon(double ox, double oy, double r,
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
	
	public static java.awt.Shape createSymbolShape(SymbolShape shape, double innerSize, double x, double y) {

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

	public static Shape getTerminatorShape(String shape) {
		float ahs = getLinkEndSize();
		float half = ahs / 2;

		switch (shape) {
		case "ARROW":
		case "ARROW OPEN":
			return new Polygon2D(
					new float[] { 0, 0 - half, 0, half }, 
					new float[] { ahs, ahs, 0, ahs  }, 4);
		case "CIRCLE":
			float radius = half;
			return new Ellipse2D.Float(- radius, - radius, radius * 2, radius * 2);
		case "GAP":
		case "NONE":
			return null;
			
		case "DIAMOND":
		case "DIAMOND OPEN":
			return  new Polygon2D(
					new float[] { 0, 0 - half, 0, half }, 
					new float[] { ahs*2, ahs, 0, ahs  }, 4);
		case "BARBED ARROW":
			// taily arrow
			GeneralPath s = new GeneralPath();
			s.moveTo(0, ahs);
			s.lineTo(0, 0);
			s.lineTo(-half, ahs);
			s.moveTo(0, 0);
			s.lineTo(half, ahs);
			s.moveTo(0, 0);
			return s;
		default:
			return null;
		}
	}
	
	public static Map<String, ConnectionTemplate> getConnectionTemplates() {
		Map<String, ConnectionTemplate> out = new LinkedHashMap<String, ConnectionTemplate>();
		out.put("BASIC", new ConnectionTemplate("NONE", "NONE", "NORMAL", "", "Link"));
		out.put("NOTE", new ConnectionTemplate("NONE", "BARBED ARROW", "DOTTED", "", "Note"));
		out.put("INHERITANCE", new ConnectionTemplate("NONE", "ARROW OPEN", "NORMAL", "", "Extension"));
		out.put("COMPOSITION", new ConnectionTemplate("NONE", "DIAMOND OPEN", "NORMAL","", "Composition"));
		out.put("CONSTRUCTION", new ConnectionTemplate("NONE", "DIAMOND", "NORMAL", "", "Construction"));
		out.put("DEPENDENCY", new ConnectionTemplate("NONE", "BARBED ARROW", "NORMAL", "", "Dependency"));
		return out;
	}

	protected static Paint getOpenTerminatorFill() {
		return Color.WHITE;
	}

	protected static float getLinkEndSize() {
		return 10;
	}
	
	protected static float getLinkGapSize() {
		return 4;
	}


	public static Set<String> getAllFlexibleShapeNames() {
		if (flexibleShapeNames == null) {
			List<String> fcs = new ArrayList<String>(FlowchartShapes.getShapes().keySet());
			List<String> umls = new ArrayList<String>(UMLShapes.getShapes().keySet());
			Collections.sort(fcs);
			Collections.sort(umls);
			flexibleShapeNames = new LinkedHashSet<String>(fcs);
			flexibleShapeNames.addAll(umls);
			flexibleShapeNames.add("CIRCLE");
			flexibleShapeNames.add("DEFAULT");
			flexibleShapeNames.add("DIAMOND");
			flexibleShapeNames.add("DIVIDER");
			flexibleShapeNames.add("ELLIPSE");
			flexibleShapeNames.add("HEXAGON");
		}
		
		return flexibleShapeNames;
	}
	
	private static Set<String> flexibleShapeNames;
	private static Map<String, FlexibleShape> flexibleShapes;
	
	public static Map<String, FlexibleShape> getFlexibleShapes() {
		if (flexibleShapes == null) {
			flexibleShapes = new LinkedHashMap<String, FlexibleShape>();
			for (String s : getAllFlexibleShapeNames()) {
				flexibleShapes.put(s, getFlexibleShape(s));
			}	
		}
		
		return flexibleShapes;
	}

	public static FlexibleShape getFlexibleShape(String style) {
		FlexibleShape border = null;
		if (style == null) {
			border = new RoundedRectFlexibleShape(20, 0, 0);
		} else {
			String ucStyle = style.toUpperCase();
			if (ucStyle.equals("DIAMOND")) {
				border = new DiamondFlexibleShape(10, 10);
			} else if (ucStyle.equals("DIVIDER")) {
				border = new DividerFlexibleShape();
			} else if (ucStyle.equals("HEXAGON")) {
				border = new HexagonFlexibleShape(20, 0, 0);
			} else if (ucStyle.equals("CIRCLE")) {
				border = new CircleFlexibleShape(0, 0);
			} else if (ucStyle.equals("ELLIPSE")) {
				border = new EllipseFlexibleShape(0, 0);
			} else if (style.startsWith("fc")) {
				return FlowchartShapes.getShape(style);
			} else if (style.startsWith("uml"))
				return UMLShapes.getShape(style);
		}
		
		if (border == null) {
			border = new RoundedRectFlexibleShape(20, 0, 0);
		}
		
		return border;
	}
	
	
}
