package org.kite9.diagram.visualization.display.style.shapes;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.position.DirectionalValues;
import org.kite9.diagram.visualization.display.style.FlexibleShape;

/**
 * Implementation of all of the flowchart-type shapes.
 * @author robmoffat
 *
 */
public class FlowchartShapes {

	static Map<String, FlexibleShape> shapes = new HashMap<String, FlexibleShape>();
	
	public static Map<String, FlexibleShape> getShapes() {
		return shapes;
	}

	static {
		shapes.put("fcPROCESS", new RoundedRectFlexibleShape(2));
		shapes.put("fcDECISION", new DiamondFlexibleShape(0, 0));
		shapes.put("fcDOCUMENT", new AbstractRectangularFlexibleShape(0,0, new DirectionalValues(0, 0, 10, 0)) {

			@Override
			protected void drawBottomShapeLeft(double x2, double y2,
					double height, GeneralPath gp) {
				Point2D p = gp.getCurrentPoint();
				double midHeight = y2 + height / 2;
				gp.lineTo(p.getX(), midHeight);
				double stretch = (p.getX() - x2) / 4;
				
				double px = x2 + stretch * 3;
				double py = y2;
				gp.curveTo(px, py, px, py, x2 + stretch * 2, midHeight);
				
				px = x2 + stretch;
				py = y2 + height;
				gp.curveTo(px, py, px, py, x2, midHeight);
				gp.lineTo(x2, y2);
			}
		});
		
		shapes.put("fcCONNECTOR", new AbstractRectangularFlexibleShape(0,0, new DirectionalValues(0, 0, 10, 0)) {

			@Override
			protected void drawBottomShapeLeft(double x2, double y2,
					double height, GeneralPath gp) {
				Point2D p = gp.getCurrentPoint();
				double midx = (p.getX() + x2) / 2;
				gp.lineTo(midx, y2+height);
				gp.lineTo(x2, y2);
			}
		});
		
		shapes.put("fcDATA", new AbstractRectangularFlexibleShape(0, 0, new DirectionalValues(0, 10, 0, 10)) {

			@Override
			protected void drawRightShapeDown(double x2, double y2, double width, GeneralPath gp) {
				gp.lineTo(x2+width, gp.getCurrentPoint().getY());
				gp.lineTo(x2, y2);
			}

			@Override
			protected void drawLeftShapeUp(double x2, double y2, double width, GeneralPath gp) {
				gp.lineTo(x2-width, gp.getCurrentPoint().getY());
				gp.lineTo(x2, y2);
			}
						
		});
		
		shapes.put("fcSTART1", new EllipseFlexibleShape(0, 0));
		
		shapes.put("fcPREDEFINED", new AbstractRectangularFlexibleShape(0, 0, new DirectionalValues(0, 10, 0, 10)) {

			@Override
			protected void drawRightShapeDown(double x2, double y2,
					double width, GeneralPath gp) {
				Point2D start = gp.getCurrentPoint();
				super.drawRightShapeDown(x2, y2, width, gp);
				gp.moveTo(start.getX(), start.getY());
				gp.lineTo(x2+width, start.getY());
				gp.lineTo(x2+width, y2);
				gp.lineTo(x2, y2);
			}

			@Override
			protected void drawLeftShapeUp(double x2, double y2, double width,
					GeneralPath gp) {
				Point2D start = gp.getCurrentPoint();
				super.drawLeftShapeUp(x2, y2, width, gp);
				gp.moveTo(start.getX(), start.getY());
				gp.lineTo(x2-width, start.getY());
				gp.lineTo(x2-width, y2);
				gp.lineTo(x2, y2);
			}
		});

		shapes.put("fcSTORED DATA",  new AbstractReservedFlexibleShape(0, 0,  new DirectionalValues(0, 10, 0, 10)) {
			
			@Override
			protected Shape getShapeInner(double x1, double y1, double x2, double y2) {
				double width = 10;
				Rectangle2D main = new Rectangle2D.Double(x1+width, y1, x2-x1-(2*width), y2-y1);
				Shape left = generateArc(x1+width, y1, width * 2.3, width, y2, false);
				Shape right = generateArc(x2-width, y1, width * 2.3, width, y2, true);
				Area a1 = new Area(main);
				a1.add(new Area(left));
				a1.add(new Area(right));
				return a1;

			}
		});
		
		
		shapes.put("fcINTERNAL", new AbstractRectangularFlexibleShape(0,0 , new DirectionalValues(5, 0, 0, 5)) {

			
			@Override
			protected void drawTopShapeRight(double x2, double y2,
					double height, GeneralPath gp) {
				double leftIndent = reserved.getLeft();
				double topIndent = reserved.getTop();
				Point2D p1 = gp.getCurrentPoint();
				gp.moveTo(p1.getX()-leftIndent, p1.getY());
				gp.lineTo(x2, y2);
				gp.moveTo(p1.getX()-leftIndent, p1.getY()-topIndent);
				gp.lineTo(x2, y2 - topIndent);
			}

			@Override
			protected void drawLeftShapeUp(double x2, double y2, double width,
					GeneralPath gp) {
				double leftIndent = reserved.getLeft();
				double topIndent = reserved.getTop();
				Point2D p = gp.getCurrentPoint();
				gp.lineTo(x2-leftIndent, p.getY());
				gp.lineTo(x2-leftIndent, y2-topIndent);
				gp.moveTo(p.getX(), p.getY());
				gp.lineTo(x2, y2-topIndent);
			}
		});
		
		shapes.put("fcSEQUENTIAL", new AbstractTopIconFlexibleShape(0, 0, new DirectionalValues(60, 0, 0, 0), 40) {

			@Override
			protected void drawTopIcon(double x1, double y1, double x2, double y2, GeneralPath gp) {
				UMLShapes.drawTopCircle(x1, y1, x2, y2, getTopIconWidth(), gp, true);
			}
		});
		
		shapes.put("fcSTART2", new AbstractTopIconFlexibleShape(0, 0, new DirectionalValues(60, 0, 0, 0), 40) {

			@Override
			protected void drawTopIcon(double x1, double y1, double x2, double y2, GeneralPath gp) {
				UMLShapes.drawTopCircle(x1, y1, x2, y2, getTopIconWidth(), gp, false);
			}
		});
		
		shapes.put("fcDIRECT", new AbstractReservedFlexibleShape(0, 0, new DirectionalValues(0, 5, 0, 5)) {
			
			@Override
			protected Shape getShapeInner(double x1, double y1, double x2, double y2) {
				Ellipse2D eleft = new Ellipse2D.Double(x1 - reserved.getLeft(), y1, reserved.getLeft()*2, y2-y1);
				Ellipse2D eright = new Ellipse2D.Double(x2-reserved.getRight(), y1, reserved.getRight()*2, y2-y1);
				Rectangle2D main = new Rectangle2D.Double(x1, y1, x2-x1, y2-y1);
				Area a1 = new Area(eleft);
				a1.add(new Area(main));
				a1.subtract(new Area(eright));
				GeneralPath p1 = new GeneralPath();
				p1.append(a1, false);
				p1.append(eright, false);
				return p1;
			}
		});
		
		shapes.put("fcDATABASE", new AbstractReservedFlexibleShape(0, 0, new DirectionalValues(30, 0, 15, 0)) {
			
			@Override
			protected Shape getShapeInner(double x1, double y1, double x2, double y2) {
				Ellipse2D etop = new Ellipse2D.Double(x1, y1, x2-x1, reserved.getTop());
				
				Ellipse2D ebottom = new Ellipse2D.Double(x1, y2-reserved.getBottom()*2, x2-x1, reserved.getBottom()*2);
				
				Rectangle2D main = new Rectangle2D.Double(x1, y1+reserved.getTop() / 2, x2-x1, y2-y1-reserved.getBottom()-reserved.getTop() /2);
				Area a1 = new Area(ebottom);
				a1.add(new Area(main));
				a1.subtract(new Area(etop));
				GeneralPath p1 = new GeneralPath();
				p1.append(a1, false);
				p1.append(etop, false);
				return p1;
			}
		});
		
		shapes.put("fcMANUAL INPUT", new AbstractRectangularFlexibleShape(0,0, new DirectionalValues(10, 0, 0 , 0)) {

			@Override
			protected void drawTopShapeRight(double x2, double y2,
					double height, GeneralPath gp) {
				gp.lineTo(x2, y2-height);
			}
		});
		
		shapes.put("fcDELAY", new TubeFlexibleShape(0, 0,false, true));
		
		shapes.put("fcDISPLAY",  new AbstractReservedFlexibleShape(0, 0,  new DirectionalValues(5, 10, 5, 10)) {
				
				@Override
				protected Shape getShapeInner(double x1, double y1, double x2, double y2) {
					double width= reserved.getRight();
					double tabSize = reserved.getLeft();
					GeneralPath out = new GeneralPath();
					out.moveTo(x1, (y1+y2)/2);
					out.lineTo(x1+tabSize, y1+reserved.getTop());
					out.lineTo(x2-tabSize, y1);
					out.lineTo(x2-tabSize, y2);
					out.lineTo(x1+tabSize, y2-reserved.getBottom());
					out.closePath();
					
					Shape right = generateArc(x2-width, y1, width * 5, width, y2, true);
					Area a1 = new Area(out);
					
					a1.add(new Area(right));
					return a1;

				}
			});
		
		shapes.put("fcPREPARATION", new HexagonFlexibleShape(10, 0, 0));
		
		shapes.put("fcMANUAL OPERATION", new AbstractRectangularFlexibleShape(0,0, new DirectionalValues(0, 10, 0 ,10)) {

			@Override
			protected void drawRightShapeDown(double x2, double y2,
					double width, GeneralPath gp) {
				Point2D p = gp.getCurrentPoint();
				gp.lineTo(p.getX() + width, p.getY());
				gp.lineTo(x2, y2);
			}
		
			@Override
			protected void drawLeftShapeUp(double x2, double y2, double width,
					GeneralPath gp) {
				gp.lineTo(x2-width, y2);
				gp.lineTo(x2, y2);
			}
		});
		
		shapes.put("fcREFERENCE", new AbstractRectangularFlexibleShape(0,0, new DirectionalValues(0, 0, 10 ,0)) {

			@Override
			protected void drawBottomShapeLeft(double x2, double y2,
					double height, GeneralPath gp) {
				Point2D p = gp.getCurrentPoint();
				gp.lineTo((p.getX() + x2)/2, p.getY()+height);
				gp.lineTo(x2, y2);
			}

			
		});
		
		shapes.put("fcTERMINATOR", new TubeFlexibleShape(0, 0, true, true));
		
		shapes.put("fcLOOP LIMIT", new AbstractRectangularFlexibleShape(0, 0, new DirectionalValues(5, 0, 0, 0)) {

			@Override
			protected void drawTopShapeRight(double x2, double y2,
					double height, GeneralPath gp) {
				Point2D p = gp.getCurrentPoint();
				gp.lineTo(p.getX() + height, p.getY() - height);
				gp.lineTo(x2 - height, p.getY() - height);
				gp.lineTo(x2, y2);
			}
		});
		
		shapes.put("fcREFERENCE", new CircleFlexibleShape(0, 0));
		
	}
	
	public static FlexibleShape getShape(String type) {
		return shapes.get(type);
	}
	
	public static Shape generateArc(double x1, double y1, double r, double w, double y2, boolean right) {
		Ellipse2D ell = new Ellipse2D.Double(x1 - w, y1, w * 2, y2-y1);
		Rectangle2D intersect = new Rectangle2D.Double(right ? x1 : x1 - 100, y1, 100, y2-y1);
		
		Area out = new Area(ell);
		out.intersect(new Area(intersect));
		return out;
	}
}
