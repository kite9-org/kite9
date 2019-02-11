package org.kite9.diagram.batik.painter;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.framework.logging.LogicException;

/**
 * This class knows how to render anything with a
 * {@link RouteRenderingInformation} item. i.e. Context edges and links. It
 * provides functionality for handling rounded corners.
 * 
 * @author robmoffat
 * 
 */
public class RoutePainterImpl {
	
	public RoutePainterImpl() {
		super();
	}
	
	public static interface EndDisplayer {
		
		/**
		 * Call this method before draw to set the position of the EndDisplayer
		 */
		public void reserve(Move m, boolean start);
		
	}
	
	public static class ReservedLengthEndDisplayer implements EndDisplayer {
		
		private final double toReserve;

		public ReservedLengthEndDisplayer(double toReserve) {
			super();
			this.toReserve = toReserve;
		}

		@Override
		public void reserve(Move m, boolean start) {
			if (start) {
				if (m.xs == m.xe) {
					if (m.ys < m.ye) {
						 m.ys += toReserve;
					} else if (m.ys > m.ye) {
						 m.ys -= toReserve;
					} else {
						throw new LogicException();
					}
				} else if (m.ys == m.ye) {
					if (m.xs < m.xe) {
						 m.xs += toReserve;
					} else if (m.xs > m.xe) {
						 m.xs -= toReserve;
					} else {
						throw new LogicException();
					}
					
				} else {
					throw new LogicException();
				}
				
				
			} else {
				if (m.xs == m.xe) {
					if (m.ys < m.ye) {
						 m.ye -= toReserve;
					} else if (m.ys > m.ye) {
						 m.ye += toReserve;
					} else {
						throw new LogicException();
					}
				} else if (m.ys == m.ye) {
					if (m.xs < m.xe) {
						 m.xe -= toReserve;
					} else if (m.xs > m.xe) {
						 m.xe += toReserve;
					} else {
						throw new LogicException();
					}
					
				} else {
					throw new LogicException();
				}
			}
		}
	}
	
	static interface LineDisplayer {
		
		public void drawMove(Move m1, Move next, Move prev, GeneralPath gp);
		
		public float cornerRadius();

	}

	public final EndDisplayer NULL_END_DISPLAYER = new EndDisplayer() {

		@Override
		public void reserve(Move m, boolean start) {
		}
	};
	
	public static class CurvedCornerHopDisplayer extends AbstractCurveCornerDisplayer {

		public CurvedCornerHopDisplayer(float radius) {
			super(radius);
		}
		
		@Override
		public void drawMove(Move m1, Move next, Move prev, GeneralPath gp) {
			if (m1.hopStart) {
				Move c = (Move) m1.clone();
				c.trim(hopSize, 0);
				drawHopStart(m1.xs, m1.ys, m1.xe, m1.ye, gp);
			}

			// draws a section of the line, plus bend into next one.

			if (m1.hopEnd) {
				Move c = (Move) m1.clone();
				c.trim(0, hopSize);
				gp.lineTo(c.xe, c.ye);
				drawHopEnd(m1.xe, m1.ye, m1.xs, m1.ys, gp);
			} else if (next != null) {
				Move c = (Move) m1.clone();
				c.trim(0, radius);
				gp.lineTo(c.xe, c.ye);
				drawCorner(gp, m1, next, m1.getDirection(), next.getDirection());
			} else {
				gp.lineTo(m1.xe, m1.ye);
			}
		}

		float hopSize = 15;

		public double drawHopStart(double x1, double y1, double x2, double y2, GeneralPath gp) {
			if (x1 < x2) {
				// hop left
				Arc2D arc = new Arc2D.Double(x1 - hopSize, y1 - hopSize, hopSize * 2, hopSize * 2, 90d, -90d,
						Arc2D.OPEN);
				gp.append(arc, true);

			} else if (x1 > x2) {

				// hop right
				Arc2D arc = new Arc2D.Double(x1 - hopSize, y1 - hopSize, hopSize * 2, hopSize * 2, 90d, 90d,
						Arc2D.OPEN);
				gp.append(arc, true);
			} else {
				return 0;
			}

			return hopSize;
		}

		public double drawHopEnd(double x1, double y1, double x2, double y2, GeneralPath gp) {
			if (x1 < x2) {
				// hop left
				Arc2D arc = new Arc2D.Double(x1 - hopSize, y1 - hopSize, hopSize * 2, hopSize * 2, 0d, 90d,
						Arc2D.OPEN);
				gp.append(arc, true);

			} else if (x1 > x2) {

				// hop right
				Arc2D arc = new Arc2D.Double(x1 - hopSize, y1 - hopSize, hopSize * 2, hopSize * 2, 180d,
						-90d, Arc2D.OPEN);
				gp.append(arc, true);
			} else {
				return 0;
			}

			return hopSize;
		}

	};
	
	public abstract static class AbstractCurveCornerDisplayer implements LineDisplayer {
		
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
			Shape s = new Arc2D.Double(tlx, tly, radius * 2, radius * 2, a1, a2,
					Arc2D.OPEN);
			gp.append(s, true);
		}

		
	};

	/**
	 * Draws the routing of the edge.
	 */
	public GeneralPath drawRouting(RouteRenderingInformation r, 
		   EndDisplayer start, EndDisplayer end, LineDisplayer line, boolean closed) {
 
		if ((r == null) || (r.getRoutePositions().size()==0)) {
			return null;
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
					gp.moveTo(a.xs, a.ys);
				} else {
					gp.moveTo(a.xs, a.ys);
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
		
		return gp;

	}

//	private void drawEnd(EndDisplayer start, boolean visible, Paint lineColour, Paint fillColour) {
//		if (isOutputting() && visible) {
//			start.draw(g2, lineColour, fillColour);
//		}
//	}

	protected void drawLength(double x1, double y1, double x2, double y2,
			GeneralPath gp, double startCrop, double endCrop, boolean start) {
		if (x1 - x2 != 0) {
			if (x1 < x2) {
				// left right arrow
				if (start)
					gp.moveTo(x1 + startCrop, y1);
				gp.lineTo(x2 - endCrop, y2);
			} else {
				// right-left arrow
				if (start)
					gp.moveTo(x1 - startCrop, y1);
				gp.lineTo(x2 + endCrop, y2);
			}
		} else {
			if (y1 < y2) {
				if (start)
					gp.moveTo(x1, y1 + startCrop);
				gp.lineTo(x2, y2 - endCrop);
			} else {
				if (start)
					gp.moveTo(x1, y1 - startCrop);
				gp.lineTo(x2, y2 + endCrop);
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

}
