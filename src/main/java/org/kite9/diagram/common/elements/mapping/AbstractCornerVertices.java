package org.kite9.diagram.common.elements.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.HPos;
import org.kite9.diagram.model.position.VPos;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

public abstract class AbstractCornerVertices implements CornerVertices {

	protected final DiagramElement rootContainer;
	private OPair<BigFraction> cx, cy;
	Collection<CornerVertices> children = new ArrayList<>(5);
	private MultiCornerVertex tl, tr, bl, br;
	private final int depth;

	public AbstractCornerVertices(DiagramElement rootContainer, OPair<BigFraction> cx, OPair<BigFraction> cy, int depth) {
		super();
		this.rootContainer = rootContainer;
		this.cx = cx;
		this.cy = cy;
		this.depth = depth;
	}

	protected void createInitialVertices(DiagramElement c) {
		tl = createVertex(BigFraction.ZERO, BigFraction.ZERO);
		tr = createVertex(BigFraction.ONE, BigFraction.ZERO);
		br = createVertex(BigFraction.ONE, BigFraction.ONE);
		bl = createVertex(BigFraction.ZERO, BigFraction.ONE);
		
		tl.addAnchor(HPos.LEFT, VPos.UP, c);
		tr.addAnchor(HPos.RIGHT, VPos.UP, c);
		bl.addAnchor(HPos.LEFT, VPos.DOWN, c);
		br.addAnchor(HPos.RIGHT, VPos.DOWN, c);
	}

	public abstract MultiCornerVertex createVertex(BigFraction x, BigFraction y);
		 
	protected final MultiCornerVertex createVertexHere(BigFraction x, BigFraction y, Map<OPair<BigFraction>, MultiCornerVertex> elements) {
		OPair<BigFraction> d = new OPair<BigFraction>(x, y);
		
		MultiCornerVertex cv = elements.get(d);
		
		if (cv == null) {
			cv = new MultiCornerVertex(getVertexIDStem(), x, y);
			elements.put(d, cv);
		}
			
		return cv;
	}

	protected String getVertexIDStem() {
		return rootContainer.getID();
	}
	
	public static BigFraction scale(BigFraction y, OPair<BigFraction> range) {
		BigFraction size = range.getB().subtract(range.getA());
		y = y.multiply(size);
		y = y.add(range.getA());
		return y;
	}

	private Collection<MultiCornerVertex> perimeterVertices = null;
	
	public Collection<MultiCornerVertex> getPerimeterVertices() {
		return perimeterVertices;
	}
	
	@Override
	public void identifyPerimeterVertices() {
		BigFraction minx = tl.getXOrdinal();
		BigFraction maxx = br.getXOrdinal();
		BigFraction miny = tl.getYOrdinal();
		BigFraction maxy = br.getYOrdinal();
		
		HashSet<MultiCornerVertex> pset = new HashSet<>(10);
		collect(minx, maxx, miny, miny, pset);
		collect(maxx, maxx, miny, maxy, pset);
		collect(minx, maxx, maxy, maxy, pset);
		collect(minx, minx, miny, maxy, pset);
		
		perimeterVertices = pset;
	}
	
	private boolean afterEq(BigFraction in, BigFraction with) {
		if (in == null) {
			return true;	
		}
		int c = in.compareTo(with);
		return c > -1;
	}
	
	private boolean beforeEq(BigFraction in, BigFraction with) {
		if (in == null) {
			return true;	
		}
		int c = in.compareTo(with);
		return c < 1;
	}
	
	private void collect(BigFraction minx, BigFraction maxx, BigFraction miny, BigFraction maxy, Collection<MultiCornerVertex> out) {
		for (MultiCornerVertex cv : getTopContainerVertices().getAllDescendentVertices()) {
			BigFraction x = cv.getXOrdinal();
			BigFraction y = cv.getYOrdinal();
			if ((afterEq(x, minx)) && (beforeEq(x, maxx)) && (afterEq(y, miny)) && (beforeEq(y, maxy))) {
				out.add(cv);
			}
		}
	}

	public Collection<MultiCornerVertex> getAllDescendentVertices() {
		Collection<MultiCornerVertex> out = new ArrayList<>();
		for (CornerVertices child : children) {
			out.addAll(child.getAllDescendentVertices());
		}
		
		return out;
	}

	
	public OPair<BigFraction> getXRange() {
		return cx;
	}

	public OPair<BigFraction> getYRange() {
		return cy;
	}
	
	protected abstract AbstractCornerVertices getTopContainerVertices();

	protected MultiCornerVertex findOverlappingVertex(MultiCornerVertex cv, RoutableHandler2D rh) {
		RoutingInfo cvRoutingInfo = cv.getRoutingInfo();
		for (MultiCornerVertex cv2 : getAllDescendentVertices()) {
			if (cv2 != cv) {
				RoutingInfo cv2routingInfo = cv2.getRoutingInfo();
				if (cv2routingInfo != null) {
					if (rh.overlaps(cvRoutingInfo, cv2routingInfo)) {
						return cv2;
					}
				}
			}
		}
		
		return null;
	}

	@Override
	public MultiCornerVertex getTopLeft() {
		return tl;
	}

	@Override
	public MultiCornerVertex getTopRight() {
		return tr;
	}

	@Override
	public MultiCornerVertex getBottomLeft() {
		return bl;
	}

	@Override
	public MultiCornerVertex getBottomRight() {
		return br;
	}

	public int getContainerDepth() {
		return depth;
	}
	

}
