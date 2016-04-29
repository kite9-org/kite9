package org.kite9.diagram.visualization.display.style.shapes;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.ext.awt.geom.Polygon2D;
import org.kite9.diagram.visualization.display.style.DirectionalValues;
import org.kite9.diagram.visualization.display.style.FlexibleShape;

/**
 * Implementation of many UML shapes.
 * @author robmoffat
 *
 */
public class UMLShapes {
	
	static Map<String, FlexibleShape> shapes = new HashMap<String, FlexibleShape>();
	
	public static Map<String, FlexibleShape> getShapes() {
		return shapes;
	}

	static {
		shapes.put("umlNOTE", new AbstractRectangularFlexibleShape(0, 0, DirectionalValues.ZERO) {
			
			double cornerSize = 10;

			@Override
			protected void drawTopShapeRight(double x2, double y2,
					double height, GeneralPath gp) {
				gp.lineTo(x2-cornerSize, y2);
				gp.lineTo(x2-cornerSize, y2+cornerSize);
				gp.lineTo(x2, y2+cornerSize);
				gp.lineTo(x2-cornerSize, y2);
				gp.lineTo(x2, y2+cornerSize);
			}
		});
		
		shapes.put("umlINTERFACE2", new AbstractTopIconFlexibleShape(0, 0, new DirectionalValues(45, 0, 0, 0), 20) {

			@Override
			protected void drawTopIcon(double x1, double y1, double x2, double y2, GeneralPath gp) {
				drawTopCircle(x1, y1, x2, y2, getTopIconWidth(), gp, false);		
			}
		});
		
		shapes.put("umlPACKAGE", new AbstractReservedFlexibleShape(0,0, new DirectionalValues(10, 0, 0, 0)) {
			
			double round = 5;
			double tabWidth = .5;
			
			@Override
			protected Shape getShapeInner(double x1, double y1, double x2, double y2) {
				RoundRectangle2D rr = new RoundRectangle2D.Double(x1, y1+reserved.getTop(), x2-x1, y2-y1-reserved.getTop(), round, round);
				double tw = (x2-x1)*tabWidth;
				
				RoundRectangle2D tab = new RoundRectangle2D.Double(x1, y1, tw, reserved.getTop()*2, round, round);
				Area out = new Area(tab);
				out.add(new Area(rr));
				return out;
			}
		});
		
		shapes.put("umlCLASS",  new RoundedRectFlexibleShape(0));
		
		shapes.put("umlINTERFACE1", new RoundedRectFlexibleShape(10));
		
		shapes.put("umlCOMPONENT", new AbstractReservedFlexibleShape(0, 0, new DirectionalValues(0, 0, 0, 20)) {
			
			double yStep = 10;
			
			@Override
			protected Shape getShapeInner(double x1, double y1, double x2,
					double y2) {
				
				double yStep = this.yStep;
				yStep = Math.min(yStep, (y2-y1) / 6);
				
				
				GeneralPath gp = new GeneralPath();
				double width = reserved.getLeft();
				width = Math.min(width, yStep*3);
				Rectangle2D r1 = new Rectangle2D.Double(x1+width / 2, y1, x2-x1-width/2, y2- y1);
				Rectangle2D r2 = new Rectangle2D.Double(x1, y1 + yStep, width, yStep);
				Rectangle2D r3 = new Rectangle2D.Double(x1 , y1 + yStep*3, width, yStep);
				Area a = new Area(r1);
				a.subtract(new Area(r2));
				a.subtract(new Area(r3));
				gp.append(a, true);
				gp.append(r2, false);
				gp.append(r3, false);
				return gp;
			}



			
			
		});
		
		shapes.put("umlCONTAINER", new AbstractReservedFlexibleShape(0, 0, new DirectionalValues(10, 10, 0, 0))  {
			
			@Override
			protected Shape getShapeInner(double x1, double y1, double x2, double y2) {
				float slanty = (float) reserved.getTop();
				float slantx = (float) reserved.getRight();
				float fx1 =  (float)x1;
				float fy1=  (float)y1;
				float fx2 =  (float)x2;
				float fy2 =  (float) y2;
				Polygon2D top = new Polygon2D(new float[] {fx1, fx1 +slantx, fx2, fx2-slantx},
											new float[] {fy1+slanty, fy1, fy1, fy1+slanty}, 4);
				Polygon2D right = new Polygon2D(new float[] {fx2, fx2, fx2 - slantx, fx2-slantx},
											new float[] {fy1, fy2 - slanty, fy2, fy1 + slanty}, 4);
				Rectangle2D main = new Rectangle2D.Float(fx1, fy1+slanty, fx2-fx1-slantx, fy2-fy1-slanty);
				GeneralPath gp = new GeneralPath();
				gp.append(top, false);
				gp.append(right, false);
				gp.append(main, false);
				return gp;
				
			}
		});		
		shapes.put("umlUSECASE", new EllipseFlexibleShape(0, 0));
		
		shapes.put("umlACTOR",  new AbstractTopIconFlexibleShape(0,0, new DirectionalValues(45, 0, 0, 0), 40d) {
			
			@Override
			protected void drawTopIcon(double x1, double y1, double x2, double y2, GeneralPath gp) {
				double minD = x2-x1;
				
				float fifth = (float) minD / 5;
				float tenth = (float) minD / 10;
				
				Area a = new Area(new Ellipse2D.Double(x1+3.5*tenth,y1, 3*tenth, 3*tenth));
				a.add(new Area(new Rectangle2D.Double(x1+fifth, y1+fifth+tenth, 3*fifth, fifth)));
				float fx1 = (float) x1;
				float fy1 = (float) y1;
				a.add(new Area(new Polygon2D(new float[] {fx1+2*fifth, fx1+3*fifth, fx1+2*fifth, fx1+1*fifth}, 
						new float[] {fy1+2*fifth, fy1+2*fifth, fy1+9*tenth, fy1+9*tenth}, 4)));
				a.add(new Area(new Polygon2D(new float[] {fx1+2*fifth, fx1+3*fifth, fx1+4*fifth, fx1+3*fifth}, 
						new float[] {fy1+2*fifth, fy1+2*fifth, fy1+9*tenth, fy1+9*tenth}, 4)));
				gp.append(a, false);
			}
		});
	}
	
	public static FlexibleShape getShape(String type) {
		return shapes.get(type);
	}
	
	protected static void drawTopCircle(double x1, double y1, double x2, double y2, double width,
			GeneralPath gp, boolean line) {
		double mx = (x2 + x1) / 2;
		double my = (y2+y1) / 2;
		
		Ellipse2D el = new Ellipse2D.Double(mx - width/2, my-width/2, width, width);
		
		Area a = new Area(el);
		if (line) {
			Rectangle2D re = new Rectangle2D.Double(mx, my+width/2-10, width/2, 10);
			a.add(new Area(re));
		}
		gp.append(a, false);
		
		gp.moveTo(x1, y1);
	}
}
