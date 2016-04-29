package org.kite9.diagram.visualization.display.components;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.adl.Link;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation.Decoration;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.PositionableDiagramElement;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.ComponentDisplayer;
import org.kite9.diagram.visualization.display.style.FixedShape;
import org.kite9.diagram.visualization.display.style.ShapeStyle;
import org.kite9.diagram.visualization.display.style.Stylesheet;
import org.kite9.diagram.visualization.format.GraphicsLayer;
import org.kite9.framework.logging.LogicException;

public class LinkDisplayer extends AbstractRouteDisplayer implements ComponentDisplayer {

	public static boolean debug = true;

	public LinkDisplayer(CompleteDisplayer parent,Stylesheet ss, GraphicsLayer g2, boolean shadow, int xo, int yo) {
		super(parent, ss, g2, shadow, xo, yo);
	}
	
	/**
	 * Extends the end of the link into the connected shape so that it meets the 
	 * perimeter of the shape
	 */
	public abstract class AbstractPerimeterAwareEndDisplayer implements EndDisplayer {
		
		private DiagramElement to;
		protected Dimension2D endPosition;
		protected Direction d;

		public AbstractPerimeterAwareEndDisplayer(DiagramElement to) {
			this.to = to;
		}

		@Override
		public void reserve(Move m, boolean start) {
			this.endPosition = start ? new Dimension2D(m.xs, m.ys) : new Dimension2D(m.xe, m.ye);
			this.d = start ? Direction.reverse(m.getDirection()) : m.getDirection();
			Shape perimeter = getPerimeterShape(to);
			Rectangle2D bounds = perimeter.getBounds2D();
			boolean canAdjust = canAdjustForPerimeter(to);
			
			if ((canAdjust) && (perimeter != null) && (bounds.getWidth() > 0) && (bounds.getHeight() > 0) && (d!=null)) {
				// need to work out how far from the move to the shape
				Area perimeterArea = new Area(perimeter);
				Shape moveProjection = projectMove(perimeter.getBounds2D());
				Area projectionArea = new Area(moveProjection);
				projectionArea.intersect(perimeterArea);
				switch (d) {
				case UP:
					adjustEndPosition(endPosition.x(), projectionArea.getBounds2D().getMaxY(), m, start);
					break;
				case DOWN:
					adjustEndPosition(endPosition.x(), projectionArea.getBounds2D().getMinY(), m, start);
					break;
				case LEFT:
					adjustEndPosition(projectionArea.getBounds2D().getMaxX(), endPosition.y(), m, start);
					break;
				case RIGHT:
					adjustEndPosition(projectionArea.getBounds2D().getMinX(), endPosition.y(), m, start);
					break;
				}
			}
			
			reserveInner(m, start);
		}

		private void adjustEndPosition(double x, double y, Move m, boolean start) {
			//System.out.println("Adjusting end to: "+x+"  "+y);
			this.endPosition = new Dimension2D(x, y);
			if (start) {
				m.xs = (float) x;
				m.ys = (float) y;
			} else {
				m.xe = (float) x;
				m.ye = (float) y;
			}
		}

		/**
		 * Creates an area representing the route the link could take if extended
		 * @param dir 
		 */
		private Shape projectMove(Rectangle2D bounds2d) {
			switch (d) {
			case UP:
				return new Rectangle2D.Double(endPosition.x(),  bounds2d.getMinY(), .01, endPosition.y()-bounds2d.getMinY());
			case DOWN:
				return new Rectangle2D.Double(endPosition.x(), endPosition.y(), .01, bounds2d.getMaxY()-endPosition.y());
			case LEFT:
				return new Rectangle2D.Double(bounds2d.getMinX(), endPosition.y(), endPosition.x() - bounds2d.getMinX() , 0.01);
			case RIGHT:
				return new Rectangle2D.Double(endPosition.x(), endPosition.y(), bounds2d.getMaxX()-endPosition.x(), 0.01);
			}
			throw new LogicException("couldn't project shape");
		}

		/**
		 * Reserve the shape area of the terminator
		 */
		protected abstract void reserveInner(Move m, boolean start);

	}

	public class StyledEndDisplayer extends AbstractPerimeterAwareEndDisplayer {

		FixedShape s;
		Shape p;

		public StyledEndDisplayer(Connected to, FixedShape s) {
			super(to);
			this.s = s;
		}

		public int draw(int x, int y, Direction d, GraphicsLayer g2, Paint lc, Paint fc) {
			if (s != null) {

				if (s.getPath() != null) {
					AffineTransform at = new AffineTransform();
					switch (d) {
						case LEFT:
							at.quadrantRotate(-1);
							break;
						case RIGHT:
							at.quadrantRotate(1);
							break;
						case UP:
							break;
						case DOWN:
							at.quadrantRotate(2);
							break;
					}
					p = at.createTransformedShape(s.getPath());
					at = new AffineTransform();
					at.translate(x+ xo, y+yo);
					p = at.createTransformedShape(p);
					if (shadow) {
						if (s.isFilled()) {
							g2.fill(p);
						} else {
							g2.draw(p);
						}
					} else {
						g2.setPaint(lc);
						g2.draw(p);
						if (s.isFilled()) {
							Paint paint = g2.getPaint();
							Paint toUse = s.getBackground(p);
							if (toUse == null) {
								toUse = fc;
							}
							g2.setPaint(toUse);
							g2.fill(p);
							g2.setPaint(paint);
						}
					}
				}

				return (int) s.getMargin().getBottom();
			} else {
				return 0;
			}
		}

		@Override
		public void draw(GraphicsLayer gp, Paint lc, Paint fc) {
			draw((int) endPosition.x(), (int) endPosition.y(), d, gp, lc, fc);
		}  

		public void reserveInner(Move m, boolean start) {
			if (s != null) {
				m.trim(start ? (float) s.getMargin().getBottom() : 0, start ? 0 : (float) s.getMargin().getBottom());
			}
		}

		public Direction getDirection() {
			return d;
		}

		public Dimension2D getPosition() {
			return endPosition;
		}

		@Override
		public Shape getTerminatorPerimeterShape() {
			return p;
		}

	};

	public final LineDisplayer LINK_HOP_DISPLAYER = new AbstractCurveCornerDisplayer(5) {

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
				gp.lineTo(c.xe + xo, c.ye + yo);
				drawHopEnd(m1.xe, m1.ye, m1.xs, m1.ys, gp);
			} else if (next != null) {
				Move c = (Move) m1.clone();
				c.trim(0, radius);
				gp.lineTo(c.xe + xo, c.ye + yo);
				drawCorner(gp, m1, next, m1.getDirection(), next.getDirection());
			} else {
				gp.lineTo(m1.xe + xo, m1.ye + yo);
			}
		}

		float hopSize = ss.getLinkHopSize();

		public double drawHopStart(double x1, double y1, double x2, double y2, GeneralPath gp) {
			if (x1 < x2) {
				// hop left
				Arc2D arc = new Arc2D.Double(x1 - hopSize + xo, y1 - hopSize + yo, hopSize * 2, hopSize * 2, 90d, -90d,
						Arc2D.OPEN);
				gp.append(arc, true);

			} else if (x1 > x2) {

				// hop right
				Arc2D arc = new Arc2D.Double(x1 - hopSize + xo, y1 - hopSize + yo, hopSize * 2, hopSize * 2, 90d, 90d,
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
				Arc2D arc = new Arc2D.Double(x1 - hopSize + xo, y1 - hopSize + yo, hopSize * 2, hopSize * 2, 0d, 90d,
						Arc2D.OPEN);
				gp.append(arc, true);

			} else if (x1 > x2) {

				// hop right
				Arc2D arc = new Arc2D.Double(x1 - hopSize + xo, y1 - hopSize + yo, hopSize * 2, hopSize * 2, 180d,
						-90d, Arc2D.OPEN);
				gp.append(arc, true);
			} else {
				return 0;
			}

			return hopSize;
		}

	};

	public void draw(DiagramElement al, RenderingInformation r) {
		Link ae = (Link) al;
		RouteRenderingInformation rr = (RouteRenderingInformation) r;
		rr.setContradicting(ae.getRenderingInformation().isContradicting() || rr.isContradicting());
		ae.setRenderingInformation(r);
		String fromEnd = ss.getLinkTerminator(ae, true);
		String toEnd = ss.getLinkTerminator(ae, false);
		
		if (rr.size()==0) {
			// && ((rr.getPerimeterPath()==null) || (rr.getPerimeterPath().length()==0))) {
			setRendered(ae, rr, false);
		} else {
			setRendered(ae, rr, true);
		}
		

		ShapeStyle drawStroke = getStyle(ae);
		double width = drawStroke.getStrokeWidth();
		StyledEndDisplayer head = new StyledEndDisplayer(ae.getFrom(), ss.getLinkTerminatorStyles().get(fromEnd));
		StyledEndDisplayer tail = new StyledEndDisplayer(ae.getTo(), ss.getLinkTerminatorStyles().get(toEnd));

		if (drawStroke != null) {
			Paint lc = (debug && ae.getRenderingInformation().isContradicting()) ? Color.RED : drawStroke.getStrokeColour();
			Paint fc = drawStroke.getBackground(null);
			drawRouting((RouteRenderingInformation) r, drawStroke.getStroke(), lc, fc, head, tail, LINK_HOP_DISPLAYER,
					false, !drawStroke.isInvisible(), width+3);
		}
		
		rr.setFromDecoration(new Decoration(fromEnd, head.getDirection(), head.getPosition()));
		rr.setToDecoration(new Decoration(toEnd, tail.getDirection(), tail.getPosition()));
	}

	private void setRendered(Link ae, RouteRenderingInformation rr, boolean state) {
		rr.setRendered(state);
		if (ae.getFromLabel()!=null) {
			ae.getFromLabel().getRenderingInformation().setRendered(state);
		}
		if (ae.getToLabel()!=null) {
			ae.getToLabel().getRenderingInformation().setRendered(state);
		}
	}


	public boolean canDisplay(DiagramElement element) {
		return (element instanceof Link);
	}

	@Override
	public double getLinkMargin(DiagramElement element, Direction d) {
		return 0; // for now
	}
	
	/**
	 * Override this method to allow {@link PerimeterAwareEndDisplayer} to work.
	 */
	protected Shape getPerimeterShape(DiagramElement de) {
		return null;
	}

	@Override
	public double getPadding(DiagramElement element, Direction d) {
		ShapeStyle ss = getStyle(element);
		double width = ss.getStrokeWidth();
		return width / 2;
	}

	private boolean canAdjustForPerimeter(DiagramElement d) {
		if (((PositionableDiagramElement) d).getRenderingInformation() instanceof RectangleRenderingInformation) {
			RectangleRenderingInformation rri =(RectangleRenderingInformation) ((PositionableDiagramElement) d).getRenderingInformation();
			return !(rri.isMultipleHorizontalLinks() && rri.isMultipleVerticalLinks());
		}
		
		return true;
	}

}
