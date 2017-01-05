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
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.visualization.planarization.grid.FracMapper;
import org.kite9.diagram.visualization.planarization.grid.FracMapperImpl;
import org.kite9.diagram.visualization.planarization.mapping.CornerVertices;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
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
	
	public void addExtraSideVertex(Connected c, Direction d, Connected to, CornerVertices cvs, Bounds x, Bounds y, List<Vertex> out, BorderTrim trim, Map<BigFraction, Double> fracMapX, Map<BigFraction, Double> fracMapY) {
		if (to != null) {
			RoutingInfo toBounds = rh.getPlacedPosition(to);
			boolean toHasCornerVertices = em.hasCornerVertices(to);
			if (!toHasCornerVertices) {
				toBounds = rh.narrow(toBounds, borderTrimAreaX, borderTrimAreaY);
			}
			BigFraction xOrd = null, yOrd = null;
			Bounds xNew = null, yNew = null;
			double fracX = 0d, fracY = 0d;
			MultiCornerVertex cvNew = null;	

			// set position
			switch (d) {
			case UP:
			case DOWN:
				x = x.narrow(rh.getBoundsOf(toBounds, true));
				final double containerWidth = x.getDistanceMax() - x.getDistanceMin();
				fracX = ((x.getDistanceCenter() - x.getDistanceMin()) / containerWidth);
				xOrd = calculateSideOrdinal(trim.xe, trim.xs, fracX, containerWidth);
				yOrd = MultiCornerVertex.getOrdForYDirection(d);
				cvNew = cvs.createVertex(xOrd, yOrd);	
				yOrd = cvNew.getYOrdinal();
				fracY = fracMapY.get(yOrd);
				
				if (toHasCornerVertices) {
					xNew = x.keep(trim.xs, trim.xe-trim.xs, fracX);	// we are connecting to a container vertex
				} else {
					xNew = x.narrow(borderTrimAreaX);
				}
				yNew = y.keep(trim.ys, trim.ye-trim.ys, fracY);
				break;
			case LEFT:
			case RIGHT:
				y = y.narrow(rh.getBoundsOf(toBounds, false));
				final double containerHeight = y.getDistanceMax() - y.getDistanceMin();
				fracY = ((y.getDistanceCenter() - y.getDistanceMin()) / containerHeight);
				yOrd = calculateSideOrdinal(trim.ye, trim.ys, fracY, containerHeight);
				xOrd = MultiCornerVertex.getOrdForXDirection(d);
				cvNew = cvs.createVertex(xOrd, yOrd);	
				xOrd = cvNew.getXOrdinal();
				fracX = fracMapX.get(xOrd);
				
				if (toHasCornerVertices) {
					yNew = y.keep(trim.ys, trim.ye-trim.ys, fracY);
				} else {
					yNew = y.narrow(borderTrimAreaY);
				}
				xNew = x.keep(trim.xs, trim.xe-trim.xs, fracX);
				break;
			}
			
			
			if (cvNew.getRoutingInfo() == null) {
				// new vertex				
				cvNew.setRoutingInfo(rh.createRouting(xNew, yNew));
				cvNew.addAnchor(HPos.getFromDirection(d), VPos.getFromDirection(d), c);
				out.add(cvNew);
			}
			
		}
	}

	private BigFraction calculateSideOrdinal(double trime, double trims, double frac, final double containerSize) {
		BigFraction yOrd;
		int denom = Math.round((float) (containerSize / (trime-trims)));
		denom = (denom % 2 == 1) ? denom + 1 : denom;  // make sure it's even
		double numerd = frac * (double) denom;
		int numer = Math.round((float) numerd);
		yOrd = BigFraction.getReducedFraction(numer, denom);
		return yOrd;
	}

	static class BorderTrim {
		double xs, ys, xe, ye;
	}
	
	private BorderTrim calculateBorderTrims(DiagramElement c) {
		BorderTrim out = new BorderTrim();
		
		int depth = em.getContainerDepth(c);
		out.xs = borderTrimAreaX - (borderTrimAreaX / (double) (depth + 1));
		out.xe = borderTrimAreaX - (borderTrimAreaX / (double) (depth + 2));
		out.ys = borderTrimAreaY - (borderTrimAreaY / (double) (depth + 1));
		out.ye = borderTrimAreaY - (borderTrimAreaY / (double) (depth + 2));
		
		return out;
	}
	
	public void setPerimeterVertexPositions(Connected before, DiagramElement c, Connected after, CornerVertices cvs, List<Vertex> out) {
		Container within = c.getContainer();
		
		Layout l = within == null ? null : within.getLayout();
	
		RoutingInfo bounds;
		Bounds bx, by;
		if (l==Layout.GRID) {
			// use the bounds of the non-grid parent container.
			Container containerWithNonGridParent = MultiCornerVertex.getRootGridContainer(c);
			bounds = rh.getPlacedPosition(containerWithNonGridParent);
			bx = rh.getBoundsOf(bounds, true);
			by = rh.getBoundsOf(bounds, false);				
		} else {
			bounds =  rh.getPlacedPosition(c);
			bx = rh.getBoundsOf(bounds, true);
			by = rh.getBoundsOf(bounds, false);
		}

		// set up frac maps to control where the vertices will be positioned
		OPair<Map<BigFraction, Double>> fracMaps = fracMapper.getFracMapForGrid(c, rh, em.getCornerVertices(c), bounds);
		Map<BigFraction, Double> fracMapX = fracMaps.getA();
		Map<BigFraction, Double> fracMapY = fracMaps.getB();
		
		addSideVertices(before, c, after, cvs, out, l, bx, by, fracMapX, fracMapY);
	
		for (MultiCornerVertex cv : cvs.getVerticesAtThisLevel()) {
			setCornerVertexRoutingAndMerge(c, cvs, cv, bx, by, out, fracMapX, fracMapY);
		}
	}

	private void addSideVertices(Connected before, DiagramElement c, Connected after, CornerVertices cvs, List<Vertex> out, Layout l, Bounds bx, Bounds by,
			Map<BigFraction, Double> fracMapX, Map<BigFraction, Double> fracMapY) {
		BorderTrim trim = calculateBorderTrims(c);
		
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
		Vertex v = em.getVertex((Connected) c);
		out.add(v);
		bounds = rh.narrow(bounds, borderTrimAreaX, borderTrimAreaY);
		v.setRoutingInfo(bounds);
	}
	
	private void setCornerVertexRoutingAndMerge(DiagramElement c, CornerVertices mergeWith, MultiCornerVertex cv, Bounds bx, Bounds by, List<Vertex> out, Map<BigFraction, Double> fracMapX, Map<BigFraction, Double> fracMapY) {
		if (cv.getRoutingInfo() == null) {
			BorderTrim trim = calculateBorderTrims(c);
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
