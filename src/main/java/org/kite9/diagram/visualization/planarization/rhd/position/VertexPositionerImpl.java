package org.kite9.diagram.visualization.planarization.rhd.position;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.MultiCornerVertex;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.elements.grid.FracMapper;
import org.kite9.diagram.common.elements.grid.FracMapperImpl;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.mapping.GridCornerVertices;
import org.kite9.diagram.common.objects.BasicBounds;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.position.VPos;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

public class VertexPositionerImpl implements Logable, VertexPositioner {

	protected ElementMapper em;
	protected FracMapper fracMapper = new FracMapperImpl();
	protected RoutableHandler2D rh;
	protected Comparator<DiagramElement> cmp;
	private Kite9Log log = new Kite9Log(this);
	
	public VertexPositionerImpl(ElementMapper em, RoutableHandler2D rh, Comparator<DiagramElement> cmp) {
		super();
		this.em = em;
		this.rh = rh;
		this.cmp = cmp;
	}

	// temp workspace
	private double borderTrimAreaX = .25d; 
	private double borderTrimAreaY = .25d; 

	public void checkMinimumGridSizes(RoutingInfo ri) {
		if (ri instanceof PositionRoutingInfo) {
			PositionRoutingInfo pri = (PositionRoutingInfo) ri;
			borderTrimAreaX = Math.min(borderTrimAreaX, pri.getWidth() /4d);
			borderTrimAreaY = Math.min(borderTrimAreaY, pri.getHeight() /4d);
		}
	}
	
	public void addExtraSideVertex(Connected c, Direction d, Connected to, CornerVertices cvs, final Bounds cx, final Bounds cy, List<Vertex> out, BorderTrim trim, Map<BigFraction, Double> fracMapX, Map<BigFraction, Double> fracMapY) {
		if (to != null) {		
			RoutingInfo toBounds = rh.getPlacedPosition(to);
			final Bounds tox = rh.getBoundsOf(toBounds, true);
			final Bounds toy = rh.getBoundsOf(toBounds, false);
			boolean toHasCornerVertices = em.hasOuterCornerVertices(to);
			MultiCornerVertex cvNew = null;	

			// set position
			switch (d) {
			case UP:
			case DOWN:
				final BigFraction yOrd = MultiCornerVertex.getOrdForYDirection(d);
				cvNew = cvs.createVertex(BigFraction.ONE_HALF, yOrd);	
				
				MultiCornerVertex left = d == Direction.UP ? cvs.getTopLeft() : cvs.getBottomLeft();
				MultiCornerVertex right = d == Direction.UP ? cvs.getTopRight() : cvs.getBottomRight();
				
				Bounds leftBounds = rh.getBoundsOf(left.getRoutingInfo(), true);
				Bounds rightBounds = rh.getBoundsOf(right.getRoutingInfo(), true);
				double gap = trim.xe - trim.xs;
				Bounds cVertexBounds = new BasicBounds(leftBounds.getDistanceMax()+gap, rightBounds.getDistanceMin()-gap);
				
				Bounds yBounds = rh.getBoundsOf(left.getRoutingInfo(), false);
				Bounds xBounds = getSideBounds(cx, tox, toHasCornerVertices, borderTrimAreaX, cVertexBounds, gap);
				
				setSideVertexRoutingInfo(c, d, out, xBounds, yBounds, cvNew);
				break;
			case LEFT:
			case RIGHT:

				final BigFraction xOrd = MultiCornerVertex.getOrdForXDirection(d);
				cvNew = cvs.createVertex(xOrd, BigFraction.ONE_HALF);	
				
				MultiCornerVertex up = d == Direction.LEFT ? cvs.getTopLeft() : cvs.getTopRight();
				MultiCornerVertex down = d == Direction.LEFT ? cvs.getBottomLeft() : cvs.getBottomRight();
				
				Bounds upBounds = rh.getBoundsOf(up.getRoutingInfo(), false);
				Bounds downBounds = rh.getBoundsOf(down.getRoutingInfo(), false);
				double gap2 = trim.ye - trim.ys;
				Bounds cVertexBounds2 = new BasicBounds(upBounds.getDistanceMax()+gap2, downBounds.getDistanceMin()-gap2);
				
				
				Bounds xBounds2 = rh.getBoundsOf(up.getRoutingInfo(), true);
				Bounds yBounds2 = getSideBounds(cy, toy, toHasCornerVertices, borderTrimAreaY, cVertexBounds2, gap2);
				
				setSideVertexRoutingInfo(c, d, out, xBounds2, yBounds2, cvNew);
			}
		}
	}

	private Bounds getSideBounds(Bounds cBounds, Bounds toDeBounds, boolean toHasCornerVertices, double trimVertex, Bounds cVertexBounds, double gap) {
		if (toHasCornerVertices) {
			// in this case, we need to find the mid-point of the common area between the two diagram-element bounds.
			Bounds out = cBounds.narrow(toDeBounds);
			double centre = out.getDistanceCenter();
			double radius = (gap)/2d;
			Bounds middle = new BasicBounds(centre-radius, centre+radius);
			return middle;
		} else {
			// in the case that we are dealing with a regular vertex, we need to be the same size as that vertex.
			Bounds toVertexBounds = toDeBounds.narrow(trimVertex);
			Bounds out = cVertexBounds.narrow(toVertexBounds);
			return out;
		}
	}

	private void setSideVertexRoutingInfo(Connected c, Direction d, List<Vertex> out, Bounds xNew, Bounds yNew, MultiCornerVertex cvNew) {
		if (cvNew.getRoutingInfo() == null) {
			// new vertex				
			cvNew.setRoutingInfo(rh.createRouting(xNew, yNew));
			cvNew.addAnchor(HPos.getFromDirection(d), VPos.getFromDirection(d), c);
			out.add(cvNew);
		}
	}

	static class BorderTrim {
		double xs, ys, xe, ye;
	}
	
	private BorderTrim calculateBorderTrims(CornerVertices c) {
		BorderTrim out = new BorderTrim();
		
		int depth = c.getContainerDepth();
		out.xs = borderTrimAreaX - (borderTrimAreaX / (double) (depth + 1));
		out.xe = borderTrimAreaX - (borderTrimAreaX / (double) (depth + 2));
		out.ys = borderTrimAreaY - (borderTrimAreaY / (double) (depth + 1));
		out.ye = borderTrimAreaY - (borderTrimAreaY / (double) (depth + 2));
		
		return out;
	}
	
	public void setPerimeterVertexPositions(Connected before, DiagramElement c, Connected after, CornerVertices cvs, List<Vertex> out) {
		final RoutingInfo bounds;
		final Bounds bx, by;
		final OPair<Map<BigFraction, Double>> fracMaps;
		
		
		if (cvs instanceof GridCornerVertices) {
			GridCornerVertices gcv = (GridCornerVertices) cvs;
			DiagramElement containerWithNonGridParent = gcv.getGridContainer();
			bounds = rh.getPlacedPosition(containerWithNonGridParent);
			bx = rh.getBoundsOf(bounds, true);
			by = rh.getBoundsOf(bounds, false);		
			fracMaps = fracMapper.getFracMapForGrid(c, rh, em.getOuterCornerVertices(containerWithNonGridParent), bounds);

		} else {
			bounds =  rh.getPlacedPosition(c);
			bx = rh.getBoundsOf(bounds, true);
			by = rh.getBoundsOf(bounds, false);
			fracMaps = fracMapper.getFracMapForGrid(c, rh, em.getOuterCornerVertices(c), bounds);
		}

		// set up frac maps to control where the vertices will be positioned
		Map<BigFraction, Double> fracMapX = fracMaps.getA();
		Map<BigFraction, Double> fracMapY = fracMaps.getB();
	
		for (MultiCornerVertex cv : cvs.getVerticesAtThisLevel()) {
			setCornerVertexRoutingAndMerge(c, cvs, cv, bx, by, out, fracMapX, fracMapY);
		}

		//addSideVertices(before, c, after, cvs, out, l, bx, by, fracMapX, fracMapY);
	}

	private void addSideVertices(Connected before, DiagramElement c, Connected after, CornerVertices cvs, List<Vertex> out, Layout l, Bounds bx, Bounds by,
			Map<BigFraction, Double> fracMapX, Map<BigFraction, Double> fracMapY) {
		BorderTrim trim = calculateBorderTrims(cvs);
		
		if (c instanceof Connected) {
			
			// add extra vertices for connections to keep the layout
			if (l != null) {
				switch (l) {
				case UP:
				case DOWN:
				case VERTICAL:
					Direction d1 = ((before != null) && (cmp.compare((Connected) c, before) == 1)) ? Direction.UP : Direction.DOWN;
					Direction d2 = ((after != null) && (cmp.compare((Connected) c, after) == 1)) ? Direction.UP : Direction.DOWN;
					addExtraSideVertex((Connected) c, d1, before, cvs, bx, by, out, trim, fracMapX, fracMapY);
					addExtraSideVertex((Connected) c, d2, after, cvs, bx, by, out, trim, fracMapX, fracMapY);
					break;
				case LEFT:
				case RIGHT:
				case HORIZONTAL:
					d1 = ((before != null) && (cmp.compare((Connected) c, before) == 1)) ? Direction.LEFT : Direction.RIGHT;
					d2 = ((after != null) && (cmp.compare((Connected) c, after) == 1)) ? Direction.LEFT : Direction.RIGHT;
					addExtraSideVertex((Connected) c, d1, before, cvs, bx, by, out, trim, fracMapX, fracMapY);
					addExtraSideVertex((Connected) c, d2, after, cvs, bx, by, out, trim, fracMapX, fracMapY);
					break;	
				default:
					// do nothing
				}
			}
		
			// add border vertices for directed edges.
			for (Connection conn : ((Connected) c).getLinks()) {
				if ((conn.getDrawDirection() != null) && (!conn.getRenderingInformation().isContradicting())) {
					Direction d = conn.getDrawDirectionFrom((Connected) c);
					addExtraSideVertex((Connected) c, d, conn.otherEnd((Connected) c), cvs, bx, by, out, trim, fracMapX, fracMapY);
				}
			}
		}
	}
	
	public void setCentralVertexPosition(DiagramElement c, List<Vertex> out) {
		RoutingInfo bounds = rh.getPlacedPosition(c);
		log.send("Placed position: "+c+" is "+bounds);
		Vertex v = em.getPlanarizationVertex((Connected) c);
		out.add(v);
		bounds = rh.narrow(bounds, borderTrimAreaX, borderTrimAreaY);
		v.setRoutingInfo(bounds);
	}
	
	private void setCornerVertexRoutingAndMerge(DiagramElement c, CornerVertices mergeWith, MultiCornerVertex cv, Bounds bx, Bounds by, List<Vertex> out, Map<BigFraction, Double> fracMapX, Map<BigFraction, Double> fracMapY) {
		if (cv.getRoutingInfo() == null) {
			BorderTrim trim = calculateBorderTrims(mergeWith);
			BigFraction xOrdinal = cv.getXOrdinal();
			BigFraction yOrdinal = cv.getYOrdinal();
			
			double xfrac = fracMapX.get(xOrdinal);
			double yfrac = fracMapY.get(yOrdinal);
			
			bx = bx.keep(trim.xs, trim.xe - trim.xs, xfrac);
			by = by.keep(trim.ys, trim.ye - trim.ys, yfrac);
			
			cv.setRoutingInfo(rh.createRouting(bx,by));
			cv = mergeWith.mergeDuplicates(cv, rh);
			
			if (cv != null) {
				out.add(cv);
				log.send("Setting routing info: "+cv+" "+bx+" "+by);
			}
		}
	}

	@Override
	public String getPrefix() {
		return "VP  ";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}


}
