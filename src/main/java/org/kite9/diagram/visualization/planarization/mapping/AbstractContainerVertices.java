package org.kite9.diagram.visualization.planarization.mapping;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.adl.Container;
import org.kite9.framework.common.Kite9ProcessingException;

import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.HIGHEST_ORD;
import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.LOWEST_ORD;

public abstract class AbstractContainerVertices implements ContainerVertices {

	private final Container c;
	private transient ArrayList<ContainerVertex> plist;
	private transient int pListSize;
	private final Map<Dimension, ContainerVertex> elements;

	private int minx, maxx, miny, maxy;

	public AbstractContainerVertices(Container c, int minx, int maxx, int miny, int maxy, Map<Dimension, ContainerVertex> elements) {
		super();
		this.c = c;
		this.minx = minx;
		this.maxx = maxx;
		this.miny = miny;
		this.maxy = maxy;
		this.elements = elements;
	}
	
	
	
	@Override 
	public ContainerVertex createVertex(int x, int y) {
		if ((x<LOWEST_ORD) || (x>HIGHEST_ORD) ||(y <LOWEST_ORD) || (y>HIGHEST_ORD)) {
			throw new Kite9ProcessingException("Out of range: "+x+" "+y);
		}
		
		x = scaleX(x);
		y = scaleY(y);
		
		Dimension d = new Dimension(x, y);
		
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
	
	protected int scaleY(int y) {
		int size = maxy - miny;
		
	}

	protected int scaleX(int x) {
		
	}


	@Override
	public ArrayList<ContainerVertex> getPerimeterVertices() {
		if (pListSize != elements.size()) {
			
			
			List<ContainerVertex> top = sort(+1, 0, collect(minx, maxy, miny, miny));
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
				int ys = ((Integer) o1.getYOrdinal()).compareTo(o2.getYOrdinal()) * yorder;
				int xs = ((Integer) o1.getXOrdinal()).compareTo(o2.getXOrdinal()) * xorder;
				
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

	private List<ContainerVertex> collect(int minx2, int maxy2, int miny2, int miny3) {
		List<ContainerVertex> out = new ArrayList<>();
		for (ContainerVertex cv : elements.values()) {
			int x = cv.getXOrdinal();
			int y = cv.getYOrdinal();
			if ((x >= minx) && (x<=maxx) && (y>=miny) && (y<=maxy)) {
				out.add(cv);
			
			}
		}
		
		return out;
	}

}
