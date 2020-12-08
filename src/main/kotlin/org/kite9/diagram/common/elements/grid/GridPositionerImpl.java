package org.kite9.diagram.common.elements.grid;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.kite9.diagram.common.elements.factory.DiagramElementFactory;
import org.kite9.diagram.common.elements.factory.TemporaryConnected;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.fraction.BigFraction;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.common.range.BasicIntegerRange;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.GridContainerPosition;
import org.kite9.diagram.common.range.IntegerRange;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;
import org.kite9.diagram.logging.Table;

/**
 * Tools for helping create Grid structure.  
 * 
 * @author robmoffat
 *
 */
public class GridPositionerImpl implements GridPositioner, Logable {
	
	private Kite9Log log = new Kite9Log(this);
	private DiagramElementFactory<?> factory;

	public GridPositionerImpl(DiagramElementFactory<?> factory) {
		this.factory = factory;
	}

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
		Map<Integer, Map<Integer, DiagramElement>> out = new HashMap<>();
		Set<Integer> xOrdinals = new TreeSet<>();
		Set<Integer> yOrdinals = new TreeSet<>();
		
		// place elements in their correct positions, as far as possible.  
		// move overlaps to array.
		for (DiagramElement diagramElement : ord.getContents()) {
			if (shoudAddToGrid(diagramElement)) {
				IntegerRange xpos = getXOccupies((Rectangular) diagramElement);
				IntegerRange ypos = getYOccupies((Rectangular) diagramElement);	
				
				if ((!IntegerRange.Companion.notSet(xpos)) && (!IntegerRange.Companion.notSet(ypos)) && (ensureGrid(out, xpos, ypos, null, xOrdinals, yOrdinals) == null)) {
					ensureGrid(out, xpos, ypos, diagramElement, xOrdinals, yOrdinals);
				} else {
					overlaps.add(diagramElement);
				}
			}
		}
		
		// add remaining/dummy elements elements, by adding extra rows if need be.
		int cell = 0;
		
		if (overlaps.size() > 0) {
			padOrdinal(xOrdinals, Math.max(1, ord.getGridColumns()));
			padOrdinal(yOrdinals, Math.max(1, ord.getGridRows()));
		}
		
		int xSize = xOrdinals.size();
		int ySize = yOrdinals.size();

		while (overlaps.size() > 0) {
			int row = Math.floorDiv(cell, xSize);
			int col = cell % xSize;
			
			List<Integer> yOrder = new ArrayList<>(yOrdinals);
			List<Integer> xOrder = new ArrayList<>(xOrdinals);
			
			if (row >= yOrder.size()) {
				ySize ++;
				yOrder.add(yOrder.stream().reduce(Math::max).orElse(0) + 1);
			}
			
			int co = xOrder.get(col);
			int ro = yOrder.get(row);

			BasicIntegerRange xpos = new BasicIntegerRange(co, co);
			BasicIntegerRange ypos = new BasicIntegerRange(ro, ro);
			
			DiagramElement d = ensureGrid(out, xpos, ypos, null, xOrdinals, yOrdinals); 
			
			if (d == null) {
				if (!overlaps.isEmpty()) {
					ensureGrid(out, xpos, ypos, overlaps.remove(0), xOrdinals, yOrdinals); 
				}
			}
			cell++;
		}
		
		ySize = removeDuplicatesAndEmptyRows(out, ySize, yOrdinals, xOrdinals);
		xSize = removeDuplicatesAndEmptyCols(out, xSize, yOrdinals, xOrdinals);
		
		// to array
		DiagramElement[][] done = yOrdinals.stream()
			.map(y -> xOrdinals.stream()
					.map(x -> out.get(y).get(x))
					.toArray(DiagramElement[]::new))
			.toArray(DiagramElement[][]::new); 
		
		fillInTheBlanks(done, ord, new ArrayList<>(xOrdinals), new ArrayList<>(yOrdinals));

		Dimension size = new Dimension(xSize, ySize);
		scaleCoordinates(done, size);
		
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


	private void padOrdinal(Set<Integer> ordinals, int s) {
		Integer max = ordinals.size() == 0 ? 0 : Collections.max(ordinals);

		while (ordinals.size() < s) {
			max = max == null ? 0 : max+1;
			ordinals.add(max);
		}
	}


	private void fillInTheBlanks(DiagramElement[][] in, Container ord, List<Integer> xOrdinals, List<Integer> yOrdinals) {
		for (int y = 0; y < in.length; y++) {
			DiagramElement[] row = in[y];
			for (int x = 0; x < row.length; x++) {
				DiagramElement toPlace = null;
				if (row[x] == null) {
					toPlace = factory.createTemporaryConnected(ord, x+"-"+y);
					((TemporaryConnected) toPlace).setContainerPosition(
							new GridContainerPosition(new BasicIntegerRange(x, x), new BasicIntegerRange(y, y)));
					modifyContainerContents(ord, toPlace);
					row[x] = toPlace;
				} else {
					toPlace = row[x];
				}
			}
		}
	}


	private int removeDuplicatesAndEmptyRows(Map<Integer, Map<Integer, DiagramElement>> out, int height, Set<Integer> yOrdinals, Set<Integer> xOrdinals) {
		List<DiagramElement> last = null;
		
		for (Iterator<Integer> yIt = yOrdinals.iterator(); yIt.hasNext();) {
			Integer y = yIt.next();
			
			List<DiagramElement> line = xOrdinals.stream()
				.map(x -> {
					Map<Integer,DiagramElement> row = out.get(y);
					return row == null ? null : row.get(x);
				})
				.collect(Collectors.toList());
			
			if ((last != null) && (last.equals(line))) {
				yIt.remove();
				height --;
			} else if (line.stream().filter(e -> e != null).count() == 0) {
				yIt.remove();
				height --;
			}
			
			last = line;
			
		}
		
		return height;
	}
	
	private int removeDuplicatesAndEmptyCols(Map<Integer, Map<Integer, DiagramElement>> out, int width, Set<Integer> yOrdinals, Set<Integer> xOrdinals) {
		List<DiagramElement> last = null;
		
		for (Iterator<Integer> xIt = xOrdinals.iterator(); xIt.hasNext();) {
			Integer x = xIt.next();
			
			List<DiagramElement> line = yOrdinals.stream()
					.map(y -> {
						Map<Integer,DiagramElement> row = out.get(y);
						return row == null ? null : row.get(x);
					})
					.collect(Collectors.toList());
			
			boolean remove = false;
			if ((last != null) && (last.equals(line))) {
				remove = true;
			}
			
			if (line.stream().filter(e -> e != null).count() == 0) {
				remove = true;
			}
			
			if (remove) {
				xIt.remove();
				width --;
			}
			
			last = line;
			
		}
		
		return width;
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
	
	private void scaleCoordinates(DiagramElement[][] grid, Dimension size) {
		Map<DiagramElement, OPair<Integer>> xp = new HashMap<>();
		Map<DiagramElement, OPair<Integer>> yp = new HashMap<>();

		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < grid[y].length; x++) {
				DiagramElement de = grid[y][x];
				
				// setup x range
				OPair<Integer> xr = xp.containsKey(de) ? xp.get(de) : new OPair<Integer>(Integer.MAX_VALUE, Integer.MIN_VALUE);
				xr = new OPair<Integer>(Math.min(x, xr.getA()), Math.max(x+1, xr.getB()));
				xp.put(de, xr);
				
				// setup y range
				OPair<Integer> yr = yp.containsKey(de) ? yp.get(de) : new OPair<Integer>(Integer.MAX_VALUE, Integer.MIN_VALUE);
				yr = new OPair<Integer>(Math.min(y, yr.getA()), Math.max(y+1, yr.getB()));
				yp.put(de, yr);
			}
		}
		
		for (DiagramElement de : xp.keySet()) {
			RectangleRenderingInformation ri = (RectangleRenderingInformation) de.getRenderingInformation();
			OPair<Integer> xr = xp.get(de);
			OPair<Integer> yr = yp.get(de);
			OPair<BigFraction> xin = new OPair<BigFraction>(BigFraction.Companion.getReducedFraction(xr.getA(), size.width), BigFraction.Companion.getReducedFraction(xr.getB().intValue(), size.width));
			OPair<BigFraction> yin = new OPair<BigFraction>(BigFraction.Companion.getReducedFraction(yr.getA(), size.height), BigFraction.Companion.getReducedFraction(yr.getB().intValue(), size.height));
			ri.setGridXPosition(xin);
			ri.setGridYPosition(yin);
			
		}
	}


	/**
	 * Iterates over the grid squares occupied by the ranges and either checks that they are empty, 
	 * or sets their value.
	 */
	private static DiagramElement ensureGrid(Map<Integer, Map<Integer, DiagramElement>> out, IntegerRange xpos, IntegerRange ypos, DiagramElement in, Set<Integer> xOrdinals, Set<Integer> yOrdinals) {
		for (int x = xpos.getFrom(); x <= xpos.getTo(); x++) {
			xOrdinals.add(x);
		}

		for (int y = ypos.getFrom(); y <= ypos.getTo(); y++) {
			yOrdinals.add(y);
		}
		
		// check that the area to place in is empty
		DiagramElement filled = null;
		for (int x = xpos.getFrom(); x <= xpos.getTo(); x++) {
			for (int y = ypos.getFrom(); y <= ypos.getTo(); y++) {
				Map<Integer, DiagramElement> row = out.get(y);
				DiagramElement f = row == null ? null : row.get(x);
				filled = filled == null ? f : filled;
			}
		}
		
		if (filled != null) {
			return filled;
		}
		
		if (in != null) {
			// place the element
			for (int x = xpos.getFrom(); x <= xpos.getTo(); x++) {
				for (int y = ypos.getFrom(); y <= ypos.getTo(); y++) {
					Map<Integer, DiagramElement> row = out.get(y);
					if (row == null) {
						row = new HashMap<>();
						out.put(y, row);
					}
					row.put(x, in);
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
