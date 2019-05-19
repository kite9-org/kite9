package org.kite9.diagram.common.elements.grid;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.GridContainerPosition;
import org.kite9.diagram.model.style.IntegerRange;
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
	
	
	private Dimension calculateGridSize(Container ord, boolean allowSpanning) {
		// these are a minimum size, but contents can exceed them and push this out.
		int xSize = ord.getGridColumns();
		int ySize = ord.getGridRows();
		
		
		// fit as many elements as possible into the grid
		for (DiagramElement diagramElement : ord.getContents()) {
			if (shoudAddToGrid(diagramElement)) {
				IntegerRange xpos = getXOccupies((Rectangular) diagramElement);
				IntegerRange ypos = getYOccupies((Rectangular) diagramElement);
				
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


	public static IntegerRange getYOccupies(Rectangular diagramElement) {
		return ((GridContainerPosition)diagramElement.getContainerPosition()).getY();
	}


	public static IntegerRange getXOccupies(Rectangular diagramElement) {
		return ((GridContainerPosition)diagramElement.getContainerPosition()).getX();
	}
	
	
	public DiagramElement[][] placeOnGrid(Container ord, boolean allowSpanning) {
		if (placed.containsKey(ord)) {
			return placed.get(ord);
		}
		
		Dimension size = calculateGridSize(ord, allowSpanning);
		
		List<DiagramElement> overlaps = new ArrayList<>();
		List<List<DiagramElement>> out = new ArrayList<>();
		
		for (int x = 0; x < size.width; x++) {
			List<DiagramElement> ys = initColumn(size.height);
			out.add(ys);
		}
		
		
		for (DiagramElement diagramElement : ord.getContents()) {
			if (shoudAddToGrid(diagramElement)) {
				IntegerRange xpos = getXOccupies((Rectangular) diagramElement);
				IntegerRange ypos = getYOccupies((Rectangular) diagramElement);	
				
				if ((!IntegerRange.notSet(xpos)) && (!IntegerRange.notSet(ypos)) && (ensureGrid(out, xpos, ypos, null, allowSpanning))) {
					ensureGrid(out, xpos, ypos, diagramElement, allowSpanning);
					int xTo = allowSpanning ? xpos.getTo() : xpos.getFrom();
					int yTo = allowSpanning ? ypos.getTo() : ypos.getFrom();
					storeCoordinates1((Connected) diagramElement, xpos.getFrom(), xTo, ypos.getFrom(), yTo);
				} else {
					overlaps.add(diagramElement);
				}
			}
		}
		
		// add remaining/dummy elements elements, by adding extra rows if need be.
		int cell = 0;
		while ((overlaps.size() > 0) || (cell < size.width * size.height)) {
			int row = Math.floorDiv(cell, size.width);
			int col = cell % size.width;
			List<DiagramElement> ys = out.get(col);
			boolean isNotSet = ys.size() < row;
			boolean isEmpty = !isNotSet && (ys.get(row) == null);
			
			if (isEmpty || isNotSet) {
				DiagramElement toPlace = overlaps.isEmpty() ?  new GridTemporaryConnected(ord, col, row) : overlaps.remove(0);
			
				if (isEmpty) {
					ys.set(row, toPlace);
				} else {
					ys.add(toPlace);
				}
				storeCoordinates1((Connected) toPlace, col, col, row, row);
			}
			cell++;
		}
		
		DiagramElement[][] done = out.stream()
			.map(l -> l.stream().toArray(DiagramElement[]::new))
			.toArray(DiagramElement[][]::new); 
		
		Arrays.stream(done).forEach(a -> Arrays.stream(a).forEach(de -> scaleCoordinates((Connected) de, size)));
		
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


	protected List<DiagramElement> initColumn(int height) {
		List<DiagramElement> ys = new ArrayList<>(height);
		for (int y = 0; y < height; y++) {
			ys.add(null);
		}
		return ys;
	}


	private boolean shoudAddToGrid(DiagramElement diagramElement) {
		return diagramElement instanceof Connected;
	}


	/**
	 * Deprecated, because we wanted to have immutable containers.
	 */
	@Deprecated
	private void modifyContainerContents(Container ord, DiagramElement d) {
		ord.getContents().add(d);
	}

	
	private void storeCoordinates1(Connected d, int sx, int ex, int sy, int ey) {
		d.getRenderingInformation().setGridXPosition(new OPair<BigFraction>(BigFraction.getReducedFraction(sx, 1), BigFraction.getReducedFraction(ex+1, 1)));
		d.getRenderingInformation().setGridYPosition(new OPair<BigFraction>(BigFraction.getReducedFraction(sy, 1), BigFraction.getReducedFraction(ey+1, 1)));
	}
	
	private void scaleCoordinates(Connected de, Dimension size) {
		RectangleRenderingInformation ri = de.getRenderingInformation();
		OPair<BigFraction> xin = ri.gridXPosition();
		OPair<BigFraction> yin = ri.gridYPosition();
		xin = new OPair<BigFraction>(BigFraction.getReducedFraction(xin.getA().intValue(), size.width), BigFraction.getReducedFraction(xin.getB().intValue(), size.width));
		yin = new OPair<BigFraction>(BigFraction.getReducedFraction(yin.getA().intValue(), size.height), BigFraction.getReducedFraction(yin.getB().intValue(), size.height));
		ri.setGridXPosition(xin);
		ri.setGridYPosition(yin);
	}


	/**
	 * Iterates over the grid squares occupied by the ranges and either checks that they are empty, 
	 * or sets their value.
	 * @param allowSpanning 
	 */
	private static boolean ensureGrid(List<List<DiagramElement>> out, IntegerRange xpos, IntegerRange ypos, DiagramElement in, boolean allowSpanning) {
		// ensure grid is large enough for the elements.
		int xTo = allowSpanning ? xpos.getTo()+1 : xpos.getFrom()+1;
		for (int x = xpos.getFrom(); x < xTo; x++) {
			List<DiagramElement> ys = out.get(x);
			
			int yTo = allowSpanning ? ypos.getTo()+1: ypos.getFrom() + 1;
			for (int y = ypos.getFrom(); y < yTo; y++) {
				if (in == null) {
					if (ys.get(y) != null) {
						return false;
					}
				} else {
					ys.set(y, in);
				}
			}
			
		}
		
		return true;
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
