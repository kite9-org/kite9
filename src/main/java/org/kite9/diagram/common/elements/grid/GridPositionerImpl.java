package org.kite9.diagram.common.elements.grid;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.dom.managers.IntegerRangeValue;
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
		
		List<DiagramElement> overlaps = new ArrayList<>();
		List<List<DiagramElement>> out = new ArrayList<>();
		
		// place elements in their correct positions, as far as possible.  
		// move overlaps to array.
		for (DiagramElement diagramElement : ord.getContents()) {
			if (shoudAddToGrid(diagramElement)) {
				IntegerRange xpos = getXOccupies((Rectangular) diagramElement);
				IntegerRange ypos = getYOccupies((Rectangular) diagramElement);	
				
				if ((!IntegerRange.notSet(xpos)) && (!IntegerRange.notSet(ypos)) && (ensureGrid(out, xpos, ypos, null) == null)) {
					ensureGrid(out, xpos, ypos, diagramElement);
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
		int ySize = out.size(); 
		int xSize = (ySize > 0) ? out.get(0).size() : 0;
		
		if ((xSize == 0) && (overlaps.size() > 0)) {
			xSize = 1;
			ySize = 1;
		}
		
		System.out.println("overlaps: "+overlaps);
		
		while ((overlaps.size() > 0) || (cell < xSize * ySize)) {
			int row = Math.floorDiv(cell, xSize);
			int col = cell % xSize;
			
			if (row >= ySize) {
				ySize ++;
			}

			IntegerRangeValue xpos = new IntegerRangeValue(col, col);
			IntegerRangeValue ypos = new IntegerRangeValue(row, row);
			
			DiagramElement d = ensureGrid(out, xpos, ypos, null); 
			
			if (d == null) {
				DiagramElement toPlace = null; 
				
				if (!overlaps.isEmpty()) {
					ensureGrid(out, xpos, ypos, overlaps.remove(0)); 
				}
			}
			cell++;
		}
		
		ySize = removeDuplicatesAndEmptyRows(out, ySize);
		out = transpose(out);
		xSize = removeDuplicatesAndEmptyRows(out, xSize);
		out = transpose(out);
		fillInTheBlanks(out, ord);
		
		// to array
		DiagramElement[][] done = out.stream()
			.map(l -> l.stream().toArray(DiagramElement[]::new))
			.toArray(DiagramElement[][]::new); 
		
		Dimension size = new Dimension(xSize, ySize);
		Arrays.stream(done).forEach(a -> Arrays.stream(a).forEach(de -> scaleCoordinates((Connected) de, size)));
		
		if (isLoggingEnabled()) {
			Table t = new Table();
			for (DiagramElement[] diagramElements : done) {
				t.addObjectRow(diagramElements);
			};
			log.send("Grid Positions: \n", t);			
		}
		
		RectangleRenderingInformation crri = ord.getRenderingInformation();
		crri.setGridXSize(size.width);
		crri.setGridYSize(size.height);
		
		
		placed.put(ord, done);
		return done;
	}
	
	private void fillInTheBlanks(List<List<DiagramElement>> in, Container ord) {
		for (int y = 0; y < in.size(); y++) {
			List<DiagramElement> row = in.get(y);
			for (int x = 0; x < row.size(); x++) {
				DiagramElement toPlace = null;
				if (row.get(x) == null) {
					toPlace = new GridTemporaryConnected(ord, x, y);
					modifyContainerContents(ord, toPlace);
					row.set(x, toPlace);
				} else {
					toPlace = row.get(x);
				}

			
				storeCoordinates1((Connected) toPlace, x, x, y, y);
			}
		}
	}


	private List<List<DiagramElement>> transpose(List<List<DiagramElement>> in) {
		List<List<DiagramElement>> out = new ArrayList<>();
		for (int y = 0; y < in.size(); y++) {
			List<DiagramElement> row = in.get(y);
			for (int x = 0; x < row.size(); x++) {
				if (out.size() <= x) {
					out.add(new ArrayList<>());
				}
				out.get(x).add(row.get(x));
			}
		}
		return out;
	}

	private int removeDuplicatesAndEmptyRows(List<List<DiagramElement>> out, int height) {
		List<DiagramElement> last = null;
		for (Iterator<List<DiagramElement>> lineIt = out.iterator(); lineIt.hasNext();) {
			List<DiagramElement> line = (List<DiagramElement>) lineIt.next();
			if ((last != null) && (last.equals(line))) {
				lineIt.remove();
				height --;
			}
			
			if (line.stream().filter(x -> x != null).count() == 0) {
				lineIt.remove();
				height --;
			}
			
		}
		return height;
	}

	protected List<DiagramElement> init(int l) {
		List<DiagramElement> ys = new ArrayList<>(l);
		for (int y = 0; y < l; y++) {
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
		System.out.println(de+" "+xin+" "+yin);
	}


	/**
	 * Iterates over the grid squares occupied by the ranges and either checks that they are empty, 
	 * or sets their value.
	 * @param allowSpanning 
	 * @param size 
	 */
	private static DiagramElement ensureGrid(List<List<DiagramElement>> out, IntegerRange xpos, IntegerRange ypos, DiagramElement in) {
		// check grid is big enough to contain this element.
		for (int y = 0; y <= ypos.getTo(); y++) {
			if (out.size() <= y) {
				out.add(new ArrayList<DiagramElement>());
			}
			
			List<DiagramElement> xs = out.get(y);
			while (xs.size() <= xpos.getTo()) {
				xs.add(null);
			}
		}
		
		// check that the area to place in is empty
		DiagramElement filled = null;
		for (int x = xpos.getFrom(); x <= xpos.getTo(); x++) {
			for (int y = ypos.getFrom(); y <= ypos.getTo(); y++) {
				filled = filled == null ? out.get(y).get(x) : filled;
			}
		}
		
		if (filled != null) {
			return filled;
		}
		
		if (in != null) {
			// place the element
			for (int x = xpos.getFrom(); x <= xpos.getTo(); x++) {
				for (int y = ypos.getFrom(); y <= ypos.getTo(); y++) {
					out.get(y).set(x, in);
				}
			}
			return in;
		} else {
			return null;
		}
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
