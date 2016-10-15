package org.kite9.diagram.visualization.planarization.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.VPos;

public abstract class AbstractContainerVertices implements ContainerVertices {

	private final Container c;
	private transient ArrayList<ContainerVertex> plist;
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
	public ArrayList<ContainerVertex> getPerimeterVertices() {
		if (pListSize != elements.size()) {
			BigFraction minx = cx.getA();
			BigFraction maxx = cx.getB();
			BigFraction miny = cy.getA();
			BigFraction maxy = cy.getB();
			
			List<ContainerVertex> top = sort(+1, 0, collect(minx, maxx, miny, miny));
			List<ContainerVertex> right = sort(0, +1, collect(maxx, maxx, miny, maxy));
			List<ContainerVertex> bottom = sort(-1, 0, collect(minx, maxx, maxy, maxy));
			List<ContainerVertex> left = sort(0, -1, collect(minx, minx, miny, maxy));
			
			plist = new ArrayList<>(top.size()+right.size()+left.size()+bottom.size());
			
			addAllExceptLast(plist, top);
			addAllExceptLast(plist, right);
			addAllExceptLast(plist, bottom);
			addAllExceptLast(plist, left);
			pListSize = elements.size();
		}
		
		return plist;
		
	}
	
	private List<ContainerVertex> sort(int xorder, int yorder, List<ContainerVertex> collect) {
		Collections.sort(collect, new Comparator<ContainerVertex>() {

			@Override
			public int compare(ContainerVertex o1, ContainerVertex o2) {
				int ys = o1.getYOrdinal().compareTo(o2.getYOrdinal()) * yorder;
				int xs = o1.getXOrdinal().compareTo(o2.getXOrdinal()) * xorder;
				
				return xs + ys;
			}
		});
		
		return collect;
	}

	/*
	 * Prevents duplicating the corner vertices
	 */
	private void addAllExceptLast(ArrayList<ContainerVertex> out, List<ContainerVertex> in) {
		for (int i = 0; i < in.size()-1; i++) {
			out.add(in.get(i));
		}
	}

	private List<ContainerVertex> collect(BigFraction minx, BigFraction maxx, BigFraction miny, BigFraction maxy) {
		List<ContainerVertex> out = new ArrayList<>();
		for (ContainerVertex cv : elements.values()) {
			BigFraction x = cv.getXOrdinal();
			BigFraction y = cv.getYOrdinal();
			if ((afterEq(x, minx)) && (beforeEq(x, maxx)) && (afterEq(y, miny)) && (beforeEq(y, maxy))) {
				out.add(cv);
			}
		}
		
		return out;
	}
	
	private boolean afterEq(BigFraction in, BigFraction with) {
		int c = in.compareTo(with);
		return c > -1;
	}
	
	private boolean beforeEq(BigFraction in, BigFraction with) {
		int c = in.compareTo(with);
		return c < 1;
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
