package org.kite9.diagram.visualization.display.java2d.adl_basic;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.adl.LinkLineStyle;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.style.ShapedDiagramElement;
import org.kite9.diagram.style.StyledDiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.java2d.style.ShapeStyle;
import org.kite9.diagram.visualization.display.java2d.style.Stylesheet;
import org.kite9.framework.logging.LogicException;

/**
 * This class knows how to render anything with a
 * {@link RouteRenderingInformation} item. i.e. Context edges and links. It
 * provides functionality for handling rounded corners.
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractRouteDisplayer extends AbstractADLDisplayer {
	
	public AbstractRouteDisplayer(CompleteDisplayer parent, Stylesheet ss, Graphics2D g2, boolean shadow,
			int xo, int yo) {
		super(parent, g2, ss, shadow, xo, yo);
	}

	public AbstractRouteDisplayer(Stylesheet ss, Graphics2D g2) {
		super(g2, ss);
	}

	static interface EndDisplayer {
		
		public void draw(Graphics2D gp, Paint lineColour, Paint fillColour);

		/**
		 * Call this method before draw to set the position of the EndDisplayer
		 */
		public void reserve(Move m, boolean start);
		
		public Shape getTerminatorPerimeterShape();
	}
	
	static interface LineDisplayer {
		
		public void drawMove(Move m1, Move next, Move prev, GeneralPath gp);
		
		public float cornerRadius();

	}

	public final EndDisplayer NULL_END_DISPLAYER = new EndDisplayer() {

		@Override
		public void draw(Graphics2D gp, Paint lineColour, Paint fillColour) {
		}

		@Override
		public void reserve(Move m, boolean start) {
		}

		@Override
		public Shape getTerminatorPerimeterShape() {
			return null;
		}
	};
	
	public abstract class AbstractCurveCornerDisplayer implements LineDisplayer {
		
		@Override
		public float cornerRadius() {
			return radius;
		}


		public AbstractCurveCornerDisplayer(float radius) {
			super();
			this.radius = radius;
		}

		float radius;

		protected void drawCorner(GeneralPath gp, Move a, Move b, Direction da, Direction db) {

			if (radius == 0) {
				return;
			}

			int cs = (int) radius;
			switch (da) {

			case RIGHT:
				if (db == Direction.UP) {
					doArcPortion(gp, a.xe - cs * 2, a.ye - cs * 2, 270, 90, radius);
				} else if (db == Direction.DOWN) {
					doArcPortion(gp, a.xe - cs * 2, a.ye, 90, -90, radius);
				}
				break;
			case LEFT:
				if (db == Direction.UP) {
					doArcPortion(gp, a.xe, a.ye - cs * 2, 270, -90, radius);
				} else if (db == Direction.DOWN) {
					doArcPortion(gp, a.xe, a.ye, 90, 90, radius);
				}
				break;
			case UP:
				if (db == Direction.LEFT) {
					doArcPortion(gp, a.xe - cs * 2, a.ye, 0, 90, radius);
				} else if (db == Direction.RIGHT) {
					doArcPortion(gp, a.xe, a.ye, 180, -90, radius);
				}
				break;
			case DOWN:
				if (db == Direction.LEFT) {
					doArcPortion(gp, a.xe - cs * 2, a.ye - cs  *2 , 0, -90, radius);
				} else if (db == Direction.RIGHT) {
					doArcPortion(gp, a.xe, a.ye - cs * 2, 180, 90, radius);
				}
				break;

			}
		}
		

		private void doArcPortion(GeneralPath gp, float tlx, float tly, float a1,
				float a2, float radius) {
			Shape s = new Arc2D.Double(tlx + xo, tly + yo, radius * 2, radius * 2, a1, a2,
					Arc2D.OPEN);
			gp.append(s, true);
		}

		
	};

	/**
	 * Draws the routing of the edge.
	 */
	protected void drawRouting(RouteRenderingInformation r, Stroke stroke,
		    Paint lineColour, Paint fillColour, EndDisplayer start, EndDisplayer end, LineDisplayer line,	boolean closed, boolean visible, double perimeterWidth) {
 
		g2.setStroke(stroke);
		g2.setPaint(shadow ? ss.getShadowColour() : lineColour);

		if ((r == null) || (r.getPositions().size()==0)) {
			return;
		}

		List<Move> moves = createMoves(r, closed);
		GeneralPath gp = new GeneralPath();
		
		for (int i = 0; i < moves.size(); i++) {
			Move a = moves.get(i);
			Move next = moves.get((i + 1) % moves.size());
			Move prev = moves.get((i + moves.size() - 1) % moves.size());
			
			if (i == 0) {
				// start
				if (!closed) {
					start.reserve(a, true);
					gp.moveTo(a.xs +xo, a.ys+yo);
				} else {
					gp.moveTo(a.xs +xo, a.ys+yo);
				}
			}  
			
			if (i == moves.size() - 1) {
				// end
				if (!closed) {
					end.reserve(a, false);
				} 
			}
			
			
			if (closed) {
				// middle
				line.drawMove(a, next, prev, gp);
			} else {
				line.drawMove(a, i == moves.size() -1 ? null : next, i == 0 ? null : prev, gp);
			}
		}

		if (closed) {
			gp.closePath();
		}
		
		
		if (isOutputting() && visible) {
			g2.draw(gp);
			drawEnd(end, visible, lineColour, fillColour);
			drawEnd(start, visible, lineColour, fillColour);
		} 
		
		if (getPathConverter() != null) {
			Move first = moves.get(0);
			r.setShapePath(getPathConverter().convert(gp, first.xs, first.ys));
			r.setPathOffset(new Dimension2D(first.xs, first.ys));
			if (perimeterWidth > 0) {
				Stroke flannelStroke = new BasicStroke((float) perimeterWidth);
				Shape flannel = flannelStroke.createStrokedShape(gp);
				Shape endShape = end.getTerminatorPerimeterShape();
				Shape startShape = start.getTerminatorPerimeterShape();
				Area a = new Area();
				if (endShape != null) {
					a.add(new Area(endShape.getBounds2D()));
				}
				if (startShape != null) {
					a.add(new Area(startShape.getBounds2D()));
				}
				a.add(new Area(flannel));
				r.setPerimeterPath(getPathConverter().convert(a, first.xs, first.ys));
			}
		}

	}

	private void drawEnd(EndDisplayer start, boolean visible, Paint lineColour, Paint fillColour) {
		if (isOutputting() && visible) {
			start.draw(g2, lineColour, fillColour);
		}
	}

	protected void drawLength(double x1, double y1, double x2, double y2,
			GeneralPath gp, double startCrop, double endCrop, boolean start) {
		if (x1 - x2 != 0) {
			if (x1 < x2) {
				// left right arrow
				if (start)
					gp.moveTo(x1 + xo + startCrop, y1 + yo);
				gp.lineTo(x2 + xo - endCrop, y2 + yo);
			} else {
				// right-left arrow
				if (start)
					gp.moveTo(x1 + xo - startCrop, y1 + yo);
				gp.lineTo(x2 + xo + endCrop, y2 + yo);
			}
		} else {
			if (y1 < y2) {
				if (start)
					gp.moveTo(x1 + xo, y1 + yo + startCrop);
				gp.lineTo(x2 + xo, y2 + yo - endCrop);
			} else {
				if (start)
					gp.moveTo(x1 + xo, y1 + yo - startCrop);
				gp.lineTo(x2 + xo, y2 + yo + endCrop);
			}
		}
	}

	/** 
	 * Moves are straight-line sections within the route
	 * 
	 * @author robmoffat
	 *
	 */
	static class Move implements Cloneable {

		float xs, ys, xe, ye;
		boolean hopStart, hopEnd;

		public Move(float x1, float y1, float x2, float y2, boolean hopStart, boolean hopEnd) {
			xs = x1;
			ys = y1;
			xe = x2;
			ye = y2;
			if ((getDirection() == Direction.LEFT) || (getDirection() == Direction.RIGHT)) {
				this.hopStart = hopStart;
				this.hopEnd = hopEnd;
			}
		}

		public void trim(float start, float end) {
			if (xs == xe) {
				if (ys > ye) {
					ys -= start;
					ye += end;
					return;
				} else if (ys < ye) {
					ys += start;
					ye -= end;
					return;
				}
			} else {
				// vertical start
				if (xs > xe) {
					xs -= start;
					xe += end;
					return;
				} else if (xs < xe) {
					xs += start;
					xe -= end;
					return;
				}
			}

			//throw new LogicException("Don't know what to do here");
		}

		public Direction getDirection() {
			if (xe > xs) {
				return Direction.RIGHT;
			} else if (xe < xs) {
				return Direction.LEFT;
			} else if (ye > ys) {
				return Direction.DOWN;
			} else if (ye < ys) {
				return Direction.UP;
			}

			return null;
		}
		
		public String toString() {
			return "("+xs+","+ys+"-"+xe+","+ye+")";
		}
		
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				throw new LogicException("Huh?", e);
			}
		}
	}

	private List<Move> createMoves(RouteRenderingInformation r, boolean closed) {
		int fx = (int) (closed ? r.getWaypoint(r.size() - 1).getWidth() : r.getWaypoint(0).getWidth());
		int fy = (int) (closed ? r.getWaypoint(r.size() - 1).getHeight() : r.getWaypoint(0).getHeight());
		boolean fh = closed ? r.isHop(r.size() -1) : r.isHop(0);
		ArrayList<Move> out = new ArrayList<Move>();
		Move a = null;
		int i = closed ? 0 : 1;
		int end = closed ? r.size() : r.size() -1;
		while (i <= end) {

			int xn = (int) r.getWaypoint(i % r.size()).getWidth();
			int yn = (int) r.getWaypoint(i % r.size()).getHeight();
			boolean hn = r.isHop(i % r.size());

			Move b = new Move(fx, fy, xn, yn, fh, hn);

			if (a != null) {
				// not the first stroke
				Direction da = a.getDirection();
				Direction db = b.getDirection();

				if ((da == db) && (!b.hopStart)) {
					// route parts in the same direction are part of the same
					// move
					b = new Move(a.xs, a.ys, b.xe, b.ye, a.hopStart, b.hopEnd);
				} else if (da == null) {
					// don't bother adding a null a
				} else if (db == null) {
					b = a;
				} else {
					out.add(a);
				}
			}

			a = b;
			i++;
			fx = xn;
			fy = yn;
			fh = hn;
		}

		if (a!=null) {
			out.add(a);
		}

		// check first and last directions
		Move first = out.get(0);
		Move last = out.get(out.size() - 1);
		if ((first.getDirection() == last.getDirection()) && closed) {
			out.remove(first);
			out.remove(last);
			out.add(new Move(last.xs, last.ys, first.xe, first.ye, first.hopEnd, last.hopStart));
		}
		
		if (!closed) {
			// trim the first and last moves so that they don't get occluded by the shape they are meeting
			
			
			
			
		}

		return out;
	}
	
	public ShapeStyle getStyle(DiagramElement de) {
		ShapeStyle toUse = ss.getLinkStyles().get(LinkLineStyle.NORMAL);
		if (de instanceof ShapedDiagramElement) {
			String shapeName = ((ShapedDiagramElement) de).getShapeName();
			if ((shapeName != null) && (ss.getLinkStyles().containsKey(shapeName))) {
				toUse= ss.getLinkStyles().get(shapeName);
			}
		}
		
		if (de instanceof StyledDiagramElement) {
			// now override if necessary
			String style = ((StyledDiagramElement) de).getStyle();
			toUse = ShapeStyle.override(style, toUse);
		}
		
		return toUse;
	}

}
