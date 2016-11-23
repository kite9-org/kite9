package org.kite9.diagram.visualization.planarization.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

public abstract class AbstractContainerVertices implements ContainerVertices {

	private final Container c;
	//private transient Set<ContainerVertex> pset;
	//private transient int pListSize;

	private OPair<BigFraction> cx, cy;
	Collection<ContainerVertices> children = new ArrayList<>(5);
	private ContainerVertex tl, tr, bl, br;

	public AbstractContainerVertices(Container c, OPair<BigFraction> cx, OPair<BigFraction> cy) {
		super();
		this.c = c;
		this.cx = cx;
		this.cy = cy;
	}

	protected void createInitialVertices(Container c) {
		tl = createVertex(BigFraction.ZERO, BigFraction.ZERO);
		tr = createVertex(BigFraction.ONE, BigFraction.ZERO);
		br = createVertex(BigFraction.ONE, BigFraction.ONE);
		bl = createVertex(BigFraction.ZERO, BigFraction.ONE);
		
		tl.addAnchor(HPos.LEFT, VPos.UP, c);
		tr.addAnchor(HPos.RIGHT, VPos.UP, c);
		bl.addAnchor(HPos.LEFT, VPos.DOWN, c);
		br.addAnchor(HPos.RIGHT, VPos.DOWN, c);
	}

	public abstract ContainerVertex createVertex(BigFraction x, BigFraction y);
	
	protected abstract ContainerVertex createVertexHere(BigFraction x, BigFraction y);
	 
	public ContainerVertex createVertexHere(BigFraction x, BigFraction y, Map<OPair<BigFraction>, ContainerVertex> elements) {
		OPair<BigFraction> d = new OPair<BigFraction>(x, y);
		
		ContainerVertex cv = getExistingVertex(d);
		
		if (cv != null) {
			// we already have this
			return cv;
		} else {
			if (cv == null) {
				cv = new ContainerVertex(c, x, y);
			}
			
			elements.put(d, cv);
			return cv;
		}
	}

	protected abstract ContainerVertex getExistingVertex(OPair<BigFraction> d);
	
	public static BigFraction scale(BigFraction y, OPair<BigFraction> range) {
		BigFraction size = range.getB().subtract(range.getA());
		y = y.multiply(size);
		y = y.add(range.getA());
		return y;
	}

	private Collection<ContainerVertex> perimeterVertices = null;
	
	public Collection<ContainerVertex> getPerimeterVertices() {
		return perimeterVertices;
	}
	
	@Override
	public void identifyPerimeterVertices(RoutableHandler2D rh) {
		Bounds minx = rh.getBoundsOf(tl.getRoutingInfo(), true);
		Bounds maxx = rh.getBoundsOf(br.getRoutingInfo(), true);
		Bounds miny = rh.getBoundsOf(tl.getRoutingInfo(), false);
		Bounds maxy = rh.getBoundsOf(br.getRoutingInfo(), false);
		
		HashSet<ContainerVertex> pset = new HashSet<>(10);
		collect(minx, maxx, miny, miny, pset, rh);
		collect(maxx, maxx, miny, maxy, pset, rh);
		collect(minx, maxx, maxy, maxy, pset, rh);
		collect(minx, minx, miny, maxy, pset, rh);
		
		perimeterVertices = pset;
	}
	
	private boolean afterEq(Bounds in, Bounds with) {
		if (in == null) {
			return true;	
		}
		int c = in.compareTo(with);
		return c > -1;
	}
	
	private boolean beforeEq(Bounds in, Bounds with) {
		if (in == null) {
			return true;	
		}
		int c = in.compareTo(with);
		return c < 1;
	}
	
	private void collect(Bounds minx, Bounds maxx, Bounds miny, Bounds maxy, Collection<ContainerVertex> out, RoutableHandler2D rh) {
		for (ContainerVertex cv : getTopContainerVertices().getAllDescendentVertices()) {
			Bounds x = rh.getBoundsOf(cv.getRoutingInfo(), true);
			Bounds y = rh.getBoundsOf(cv.getRoutingInfo(), false);
			if ((afterEq(x, minx)) && (beforeEq(x, maxx)) && (afterEq(y, miny)) && (beforeEq(y, maxy))) {
				out.add(cv);
			}
		}
	}

	public Collection<ContainerVertex> getAllDescendentVertices() {
		Collection<ContainerVertex> out = new ArrayList<>();
		for (ContainerVertices child : children) {
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
	
	protected abstract AbstractContainerVertices getTopContainerVertices();

	protected ContainerVertex findOverlappingVertex(ContainerVertex cv, RoutableHandler2D rh) {
		RoutingInfo cvRoutingInfo = cv.getRoutingInfo();
		for (ContainerVertex cv2 : getAllDescendentVertices()) {
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

	
}
