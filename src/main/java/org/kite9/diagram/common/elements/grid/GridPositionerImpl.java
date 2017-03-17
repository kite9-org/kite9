package org.kite9.diagram.common.elements.grid;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.common.elements.MultiCornerVertex;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.GridContainerPosition;
import org.kite9.diagram.model.style.IntegerRange;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.Table;

/**
 * Tools for helping create Grid structure.  
 * 
 * @author robmoffat
 *
 */
public class GridPositionerImpl implements GridPositioner, Logable {
	
	private Kite9Log log = new Kite9Log(this);

	Map<Container, DiagramElement[][]> placed = new HashMap<>();
	Map<DiagramElement, OPair<BigFraction>> xPositions = new HashMap<>(100);
	Map<DiagramElement, OPair<BigFraction>> yPositions = new HashMap<>(100);
	
	
	private Dimension calculateGridSize(Container ord, boolean allowSpanning) {
		// these are a minimum size, but contents can exceed them and push this out.
		int xSize = (int) ord.getGridColumns();
		int ySize = (int) ord.getGridRows();
		
		
		// fit as many elements as possible into the grid
		for (DiagramElement diagramElement : ord.getContents()) {
			if (diagramElement instanceof Connected) {
				IntegerRange xpos = getXOccupies((Connected) diagramElement);
				IntegerRange ypos = getYOccupies((Connected) diagramElement);
				
				if ((xpos != null) && (ypos != null)) {
					if (allowSpanning) {
						xSize = Math.max(xpos.getTo()+1, xSize);
						ySize = Math.max(ypos.getTo()+1, ySize);
					} else {
						xSize = Math.max(xpos.getFrom()+1, xSize);
						ySize = Math.max(ypos.getFrom()+1, ySize);
					}
				}
			}
		}
		
		return new Dimension(xSize, ySize);
	}


	public static IntegerRange getYOccupies(Connected diagramElement) {
		return ((GridContainerPosition)diagramElement.getContainerPosition()).getY();
	}


	public static IntegerRange getXOccupies(Connected diagramElement) {
		return ((GridContainerPosition)diagramElement.getContainerPosition()).getX();
	}
	
	
	public DiagramElement[][] placeOnGrid(Container ord, boolean allowSpanning) {
		if (placed.containsKey(ord)) {
			return placed.get(ord);
		}
		
		Dimension size = calculateGridSize(ord, allowSpanning);
		
		List<DiagramElement> overlaps = new ArrayList<>();
		List<DiagramElement[]> out = new ArrayList<>();
		
		for (int x = 0; x < size.width; x++) {
			DiagramElement[] ys = new DiagramElement[size.height];
			out.add(ys);
		}
		
		
		for (DiagramElement diagramElement : ord.getContents()) {
			if (diagramElement instanceof Connected) {
				IntegerRange xpos = getXOccupies((Connected) diagramElement);
				IntegerRange ypos = getYOccupies((Connected) diagramElement);	
				
				if ((!IntegerRange.notSet(xpos)) && (!IntegerRange.notSet(ypos)) && (ensureGrid(out, xpos, ypos, null, allowSpanning))) {
					ensureGrid(out, xpos, ypos, diagramElement, allowSpanning);
					int xTo = allowSpanning ? xpos.getTo() : xpos.getFrom();
					int yTo = allowSpanning ? ypos.getTo() : ypos.getFrom();
					storeCoordinates1(diagramElement, xpos.getFrom(), xTo, ypos.getFrom(), yTo);
				} else {
					overlaps.add(diagramElement);
				}
			}
		}
		
		// add remaining/dummy elements elements, by adding extra rows if need be.
		int xr = 0;
		while ((overlaps.size() > 0) || (xr < out.size())) {
			// add another (empty) column
			if (xr == out.size()) {
				DiagramElement[] ys = new DiagramElement[size.height];
				out.add(ys);
				size = new Dimension(size.width+1, size.height);
			}
			
			DiagramElement[] ys = out.get(xr);
			for (int y = 0; y < size.height; y++) {
				if (ys[y] == null) {
					if (!overlaps.isEmpty()) {
						ys[y] = overlaps.remove(overlaps.size()-1);
					} else {
						ys[y] = new GridTemporaryConnected(ord, xr, y);
						modifyContainerContents(ord, ys[y]);
					}
					storeCoordinates1(ys[y], xr, xr, y, y);
				}
			}
		
			xr++;
		}
		
		DiagramElement[][] done = (DiagramElement[][]) out.toArray(new DiagramElement[out.size()][]);
		
		for (DiagramElement de : ord.getContents()) {
			if (de instanceof Connected) {
				scaleCoordinates(de, size);
			}
		}
		
		if (isLoggingEnabled()) {
			Table t = new Table();
			for (DiagramElement[] diagramElements : done) {
				t.addObjectRow(diagramElements);
			};
			log.send("Grid Positions (transposed axes): \n", t);			
		}
		
		
		
		placed.put(ord, done);
		return done;
	}


	/**
	 * Deprecated, because we wanted to have immutable containers.
	 */
	@Deprecated
	private void modifyContainerContents(Container ord, DiagramElement d) {
		ord.getContents().add(d);
	}

	
	private void storeCoordinates1(DiagramElement diagramElement, int sx, int ex, int sy, int ey) {
		xPositions.put(diagramElement, new OPair<BigFraction>(BigFraction.getReducedFraction(sx, 1), BigFraction.getReducedFraction(ex+1, 1)));
		yPositions.put(diagramElement, new OPair<BigFraction>(BigFraction.getReducedFraction(sy, 1), BigFraction.getReducedFraction(ey+1, 1)));
	}
	
	private void scaleCoordinates(DiagramElement de, Dimension size) {
		OPair<BigFraction> xin = xPositions.get(de);
		OPair<BigFraction> yin = yPositions.get(de);
		xin = new OPair<BigFraction>(BigFraction.getReducedFraction(xin.getA().intValue(), size.width), BigFraction.getReducedFraction(xin.getB().intValue(), size.width));
		yin = new OPair<BigFraction>(BigFraction.getReducedFraction(yin.getA().intValue(), size.height), BigFraction.getReducedFraction(yin.getB().intValue(), size.height));
		xPositions.put(de, xin);
		yPositions.put(de, yin);
	}


	/**
	 * Iterates over the grid squares occupied by the ranges and either checks that they are empty, 
	 * or sets their value.
	 * @param allowSpanning 
	 */
	private static boolean ensureGrid(List<DiagramElement[]> out, IntegerRange xpos, IntegerRange ypos, DiagramElement in, boolean allowSpanning) {
		// ensure grid is large enough for the elements.
		int xTo = allowSpanning ? xpos.getTo()+1 : xpos.getFrom()+1;
		for (int x = xpos.getFrom(); x < xTo; x++) {
			DiagramElement[] ys = out.get(x);
			
			int yTo = allowSpanning ? ypos.getTo()+1: ypos.getFrom() + 1;
			for (int y = ypos.getFrom(); y < yTo; y++) {
				if (in == null) {
					if (ys[y] != null) {
						return false;
					}
				} else {
					ys[y] = in;
				}
			}
			
		}
		
		return true;
	}


	@Override
	public OPair<BigFraction> getGridXPosition(DiagramElement elem) {
		if (!xPositions.containsKey(elem)) {
			throw new Kite9ProcessingException("Would expect the element to have been given a grid position: "+elem);
		}
		
		return xPositions.get(elem);
	}


	@Override
	public OPair<BigFraction> getGridYPosition(DiagramElement elem) {
		if (!yPositions.containsKey(elem)) {
			throw new Kite9ProcessingException("Would expect the element to have been given a grid position: "+elem);
		}
		
		return yPositions.get(elem);
	}


	public List<MultiCornerVertex> getClockwiseOrderedContainerVertices(CornerVertices cvs) {
		BigFraction minx = null;
		BigFraction maxx = null;
		BigFraction miny = null;
		BigFraction maxy = null;
			
		cvs.identifyPerimeterVertices();
		
		Collection<MultiCornerVertex> perimeterVertices = cvs.getPerimeterVertices();
		for (MultiCornerVertex cv : perimeterVertices) {
			BigFraction	xb = cv.getXOrdinal();
			BigFraction yb = cv.getYOrdinal();
			
			minx = limit(minx, xb, -1);
			miny = limit(miny, yb, -1);
			maxx = limit(maxx, xb, 1);
			maxy = limit(maxy, yb, 1);		
		}
			
		List<MultiCornerVertex> top = sort(+1, 0, collect(minx, maxx, miny, miny, perimeterVertices));
		List<MultiCornerVertex> right = sort(0, +1, collect(maxx, maxx, miny, maxy, perimeterVertices));
		List<MultiCornerVertex> bottom = sort(-1, 0, collect(minx, maxx, maxy, maxy, perimeterVertices));
		List<MultiCornerVertex> left = sort(0, -1, collect(minx, minx, miny, maxy, perimeterVertices));
		
		List<MultiCornerVertex> plist = new ArrayList<>(top.size()+right.size()+left.size()+bottom.size());
		
		addAllExceptLast(plist, top);
		addAllExceptLast(plist, right);
		addAllExceptLast(plist, bottom);
		addAllExceptLast(plist, left);
	
		return plist;
	}
	

	private BigFraction limit(BigFraction current, BigFraction in, int compare) {
		if ((current == null) || (in.compareTo(current) == compare)) {
			current = in;
		}
		return current;
	}
	
	private List<MultiCornerVertex> sort(int xorder, int yorder, List<MultiCornerVertex> collect) {
		Collections.sort(collect, new Comparator<MultiCornerVertex>() {

			@Override
			public int compare(MultiCornerVertex o1, MultiCornerVertex o2) {
				
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
	private void addAllExceptLast(List<MultiCornerVertex> out, List<MultiCornerVertex> in) {
		for (int i = 0; i < in.size()-1; i++) {
			out.add(in.get(i));
		}
	}

	private List<MultiCornerVertex> collect(BigFraction minx, BigFraction maxx, BigFraction miny, BigFraction maxy, Collection<MultiCornerVertex> elements) {
		List<MultiCornerVertex> out = new ArrayList<>();
		for (MultiCornerVertex cv : elements) {
			BigFraction xb = cv.getXOrdinal();
			BigFraction yb = cv.getYOrdinal();
			
			if ((minx.compareTo(xb) != 1) && (maxx.compareTo(xb) != -1)
				&& (miny.compareTo(yb) != 1) && (maxy.compareTo(yb) != -1)) {
					out.add(cv);
				}
		}
		
		return out;
	}


	@Override
	public String getPrefix() {
		return "GP  ";
	}


	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
}
