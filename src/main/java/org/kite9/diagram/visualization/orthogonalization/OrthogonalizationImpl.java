package org.kite9.diagram.visualization.orthogonalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.CompactionHelperVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;

/**
 * This class manages the relationships of the {@link Dart}s to the {@link Edge}s, and keeps track of
 * any inserted temporary vertex objects.
 * 
 * @author robmoffat
 *
 */
public class OrthogonalizationImpl implements Orthogonalization {

    private static final long serialVersionUID = 7718144529742941851L;

	public OrthogonalizationImpl() {
	}
	
	public OrthogonalizationImpl(Planarization pln) {
		this.allVertices = new LinkedHashSet<Vertex>();
		this.pln = pln;
	}
 
	protected Planarization pln;
	
	public Planarization getPlanarization() {
		return pln;
	}

	/**
	 * The list of darts for each vertex should be ordered clockwise from the top 
	 * left corner
	 */
	protected Map<Vertex, List<Dart>> dartOrdering = new HashMap<Vertex, List<Dart>>();

	/**
	 * Stores the list of darts for a diagram element
	 */
	protected Map<DiagramElement, Set<Dart>> waypointMap = new HashMap<>();
	
	protected Set<Dart> allDarts = new LinkedHashSet<Dart>();							// needed for compaction
	
	protected Collection<Vertex> allVertices;												// needed for compaction
	
	protected List<DartFace> faces = new ArrayList<DartFace>();						// needed for compaction
	
	protected Map<Face, DartFace> dartFaceMap = new HashMap<>();
	
	protected Map<Rectangular, List<DartFace>> facesRectangularMap = new HashMap<>();
	
	public Set<Dart> getDartsForEdge(DiagramElement e) {
		return waypointMap.get(e);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ORTHOGONALIZATION: [ ");
		sb.append("\nfaces: \n"+displayFaces(faces,"\n\t", "\n\n"));
		return sb.toString();
	}

	private String displayFaces(List<DartFace> faces2, String linesep, String facesep) {
		StringBuilder sb = new StringBuilder();
		for (DartFace x : faces2) {
			sb.append(facesep);
			sb.append(x.getId());
			sb.append(":");
			sb.append(renderFaceDarts(x.getDartsInFace(), linesep));
		}
		return sb.toString();
	}

	public static String renderFaceDarts(List<DartDirection> x, String sep) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < x.size(); i++) {
			DartDirection out = x.get(i);
			sb.append(out.toString());
			sb.append(sep);
		}
		return sb.toString();
	}

	public Map<Vertex, List<Dart>> getDartOrdering() {
		return dartOrdering;
	}

	public Set<Dart> getAllDarts() {
		return allDarts;
	}

	public Collection<Vertex> getAllVertices() {
		return allVertices;
	}

	public List<DartFace> getFaces() {
		return faces;
	}

	// a list of darts with corners following them, used for flow orthogonalization
	public Map<Vertex, List<Dart>> cornerDarts = new HashMap<Vertex, List<Dart>>();

	int nextDart = 0;
	
	private Map<Vertex,Map<Vertex, Set<Dart>>> existingDarts = new HashMap<Vertex, Map<Vertex, Set<Dart>>>();
	
	public Dart createDart(Vertex from, Vertex to, DiagramElement partOf, Direction d, Direction partOfSide) {
		return createDart(from, to, Collections.singleton(partOf), d, partOfSide);
	}
	
	public Dart createDart(Vertex from, Vertex to, Set<DiagramElement> partOf, Direction d, Direction partOfSide) {
		Map<DiagramElement, Direction> next = new HashMap<>();
		partOf.forEach(a -> next.put(a, partOfSide));
		return createDart(from, to, next, d);
	}
	

	public Dart createDart(Vertex from, Vertex to, Map<DiagramElement, Direction> partOf, Direction d) {
		Vertex first = from.compareTo(to)>0 ? from : to;
		Vertex second = first == from ? to : from;
		
		partOf.remove(null);
		
		// first, check if dart already exists, or one has been created for this purpose already.
		Set<Dart> existing = null;
		
		Map<Vertex, Set<Dart>> secMap = existingDarts.get(first);
		if (secMap==null) {
			secMap = new HashMap<Vertex, Set<Dart>>();
			existingDarts.put(first, secMap);
		} 
		
		
		existing = secMap.get(second);
		if (existing==null) {
			existing = new LinkedHashSet<Dart>();
			secMap.put(second, existing);
		} else {
			// we potentially have some darts that could be used instead
			for (Dart dart : existing) {
				if (dart.getDrawDirectionFrom(from)!=d) {
					throw new LogicException("Trying to create new dart in different direction to: "+existing);				
				}
				
				// add some new underlyings
				((DartImpl)dart).underlyings.putAll(partOf);
				addToWaypointMap(dart, partOf.keySet());
				return dart;
			}
		}
		
		// need to create the dart
		ensureNoDartInDirection(from, d);
		ensureNoDartInDirection(to, Direction.reverse(d));
		Dart out = new DartImpl(from, to, partOf, d, "d"+nextDart++, this);
		addToWaypointMap(out, partOf.keySet());
		existing.add(out);
		allDarts.add(out);
		allVertices.add(from);
		allVertices.add(to);
		return out;
		
	}
	
	private void ensureNoDartInDirection(Vertex from, Direction d) {
		if (getDartInDirection(from, d ) != null) {
			throw new Kite9ProcessingException("Already have a dart going "+d+" from "+from);
		}
	}
	
	private Dart getDartInDirection(Vertex around, Direction d) {
		for (Edge e : around.getEdges()) {
			if (e instanceof Dart) {
				if (e.getDrawDirectionFrom(around) == d) {
					return (Dart) e;
				}
			}
		}
		
		return null;
	}

	private void addToWaypointMap(Dart out, Set<DiagramElement> partsOf) {
		for (DiagramElement partOf : partsOf) {
			Set<Dart> wpDarts = waypointMap.get(partOf);
			if (wpDarts == null) {
				wpDarts = new LinkedHashSet<>();
				waypointMap.put(partOf, wpDarts);
			}
			
			wpDarts.add(out);
		}
	}

	void unlinkDartFromMap(Dart d) {
		Vertex from = d.getFrom();
		Vertex to = d.getTo();
		Vertex first = from.compareTo(to)>0 ? from : to;
		Vertex second = first == from ? to : from;
		
		Set<Dart> existing = null;
		
		Map<Vertex, Set<Dart>> secMap = existingDarts.get(first);
		if (secMap==null) {
			throw new LogicException("Dart is not in map: "+d);
		} else {
			existing = secMap.get(second);			
			if (!existing.contains(d)) {
				throw new LogicException("Dart is not in map: "+d);				
			} else {
				existing.remove(d);
			}
		}
	}
	
	void relinkDartInMap(Dart d) {
		Vertex from = d.getFrom();
		Vertex to = d.getTo();
		Vertex first = from.compareTo(to)>0 ? from : to;
		Vertex second = first == from ? to : from;
	
		Map<Vertex, Set<Dart>> secMap = existingDarts.get(first);
		if (secMap==null) {
			secMap = new HashMap<Vertex, Set<Dart>>();
			existingDarts.put(first, secMap);		
		} 
				
		Set<Dart> theSet = secMap.get(second);
		if (theSet==null) {
			theSet = new UnorderedSet<Dart>();
			secMap.put(second, theSet);
		}
		
		theSet.add(d);
	}

	private int faceNo = 0;
	
	public DartFace createDartFace(Rectangular partOf, boolean outerFace, List<DartDirection> darts) {
		DartFace df = new DartFace(faceNo++, outerFace, darts);
		faces.add(df);
		List<DartFace> frl = facesRectangularMap.get(partOf);
		if (frl == null) {
			frl = new ArrayList<>();
			facesRectangularMap.put(partOf, frl);
		}
		frl.add(df);
		return df;
	}

	private int helper= 0;
	
	public Vertex createHelperVertex() {
		Vertex out = new CompactionHelperVertex("x"+helper++);
		allVertices.add(out);
		return out;
	}

	/**
	 * The list of vertices must be returned in the same order as the connection represented.
	 */
	@Override
	public List<Vertex> getWaypointsForBiDirectional(Connection e) {
		Set<Dart> darts = getDartsForEdge(e);
		if (darts == null) {
			return null;
		} else {
			Map<Vertex, Long> vertexCounts = darts.stream()
					.flatMap(d -> Stream.of(d.getFrom(), d.getTo()))
					.collect(Collectors.groupingBy(a -> a, Collectors.counting()));
					
			Set<Vertex> ends = vertexCounts.keySet().stream().filter(a -> vertexCounts.get(a) == 1).collect(Collectors.toSet());		
					
			if (ends.size() != 2) {
				throw new Kite9ProcessingException();
			}
			
			
			Iterator<Vertex> it = ends.iterator();
			Vertex one = it.next();
			Vertex two = it.next();
			Vertex start, end;
			
			if ((one.getDiagramElements().contains(e.getFrom())) && (two.getDiagramElements().contains(e.getTo()))) {
				start = one;
				end = two;
			} else if  ((one.getDiagramElements().contains(e.getTo())) && (two.getDiagramElements().contains(e.getFrom()))) {
				start = two;
				end = one;
			} else {
				throw new Kite9ProcessingException();
			}
		
			List<Vertex> out = new ArrayList<>(darts.size()+1);
			Dart next = null;
			do {
				out.add(start);
				
				if (start == end) {
					return out;
				}
				
				next = findNextDart(darts, start, next);
				start = next.otherEnd(start);
			} while (true);
		}
	}

	private Dart findNextDart(Set<Dart> darts, Vertex start, Dart lastDart) {
		for (Edge e : start.getEdges()) {
			if ((darts.contains(e)) && (e!=lastDart)) {
				return (Dart) e;
			}
		}
		
		throw new LogicException("Couldn't find next dart from "+start);
	}

	@Override
	public Set<Dart> getDartsForDiagramElement(DiagramElement e) {
		return waypointMap.get(e);
	}

	@Override
	public List<DartFace> getDartFacesForRectangular(Rectangular r) {
		return facesRectangularMap.getOrDefault(r, Collections.emptyList());
	}
}
