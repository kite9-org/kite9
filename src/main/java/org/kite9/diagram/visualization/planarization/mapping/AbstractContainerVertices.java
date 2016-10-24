package org.kite9.diagram.visualization.planarization.mapping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.VPos;

public abstract class AbstractContainerVertices implements ContainerVertices {

	private final Container c;
	private transient Set<ContainerVertex> pset;
	private transient int pListSize;
	final Map<OPair<BigFraction>, ContainerVertex> elements;

	private OPair<BigFraction> cx, cy;

	public AbstractContainerVertices(Container c, OPair<BigFraction> cx, OPair<BigFraction> cy, Map<OPair<BigFraction>, ContainerVertex> elements) {
		super();
		this.c = c;
		this.cx = cx;
		this.cy = cy;
		this.elements = elements;
		
		ContainerVertex tl = createVertex(BigFraction.ZERO, BigFraction.ZERO);
		ContainerVertex tr = createVertex(BigFraction.ONE, BigFraction.ZERO);
		ContainerVertex br = createVertex(BigFraction.ONE, BigFraction.ONE);
		ContainerVertex bl = createVertex(BigFraction.ZERO, BigFraction.ONE);
		
		tl.addAnchor(HPos.LEFT, VPos.UP, c);
		tr.addAnchor(HPos.RIGHT, VPos.UP, c);
		bl.addAnchor(HPos.LEFT, VPos.DOWN, c);
		br.addAnchor(HPos.RIGHT, VPos.DOWN, c);
	}
	
	
	
	@Override 
	public ContainerVertex createVertex(BigFraction x, BigFraction y) {
		x = scale(x, cx);
		y = scale(y, cy);
		
		OPair<BigFraction> d = new OPair<BigFraction>(x, y);
		
		ContainerVertex cv = elements.get(d);
		
		if (cv != null) {
			// we already have this
			return cv;
		} else {
			cv = new ContainerVertex(c, x, y);
			elements.put(d, cv);
			return cv;
		}
		
	}
	
	public static BigFraction scale(BigFraction y, OPair<BigFraction> range) {
		BigFraction size = range.getB().subtract(range.getA());
		y = y.multiply(size);
		y = y.add(range.getA());
		return y;
	}

	@Override
	public Collection<ContainerVertex> getPerimeterVertices() {
		if (pListSize != elements.size()) {
			BigFraction minx = cx.getA();
			BigFraction maxx = cx.getB();
			BigFraction miny = cy.getA();
			BigFraction maxy = cy.getB();
			
			pset = new HashSet<>(10);
			collect(minx, maxx, miny, miny, pset);
			collect(maxx, maxx, miny, maxy, pset);
			collect(minx, maxx, maxy, maxy, pset);
			collect(minx, minx, miny, maxy, pset);
			
			
			pListSize = elements.size();
		}
		
		return pset;
		
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
	
	private void collect(BigFraction minx, BigFraction maxx, BigFraction miny, BigFraction maxy, Collection<ContainerVertex> out) {
		for (ContainerVertex cv : elements.values()) {
			BigFraction x = cv.getXOrdinal();
			BigFraction y = cv.getYOrdinal();
			if ((afterEq(x, minx)) && (beforeEq(x, maxx)) && (afterEq(y, miny)) && (beforeEq(y, maxy))) {
				out.add(cv);
			}
		}
	}

	public OPair<BigFraction> getXRange() {
		return cx;
	}



	public OPair<BigFraction> getYRange() {
		return cy;
	}



	@Override
	public Collection<ContainerVertex> getAllVertices() {
		return elements.values();
	}

}
