package org.kite9.diagram.visualization.orthogonalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.CompactionHelperVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
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
		this.allVertices = new ArrayList<Vertex>(pln.getAllVertices());
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
			sb.append(renderFaceDarts(x.dartsInFace, linesep));
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
	
	public Dart createDart(Vertex from, Vertex to, DiagramElement partOf, Direction d) {
		Vertex first = from.compareTo(to)>0 ? from : to;
		Vertex second = first == from ? to : from;
		
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
				((DartImpl)dart).underlyings.add(partOf);
				addToWaypointMap(dart, partOf);
				return dart;
			}
		}
		
		// need to create the dart
		ensureNoDartInDirection(from, d);
		ensureNoDartInDirection(to, d);
		Dart out = new DartImpl(from, to, partOf, d, "d"+nextDart++, this);
		addToWaypointMap(out, partOf);
		existing.add(out);
		allDarts.add(out);
		return out;
		
	}
	
	private void ensureNoDartInDirection(Vertex from, Direction d) {
		if (getDartInDirection(from, d) != null) {
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

	private void addToWaypointMap(Dart out, DiagramElement partOf) {
		Set<Dart> wpDarts = waypointMap.get(partOf);
		if (wpDarts == null) {
			wpDarts = new LinkedHashSet<>();
			waypointMap.put(partOf, wpDarts);
		}
		
		wpDarts.add(out);
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

	public DartFace createDartFace(Face f) {
		DartFace df = new DartFace(f, f.isOuterFace());
		faces.add(df);
		dartFaceMap.put(f, df);
		return df;
	}

	private int helper= 0;
	
	public Vertex createHelperVertex() {
		Vertex out = new CompactionHelperVertex("x"+helper++);
		allVertices.add(out);
		return out;
	}

	@Override
	public DartFace getDartFaceForFace(Face f) {
		return dartFaceMap.get(f);
	}

	@Override
	public List<Vertex> getWaypointsForBiDirectional(DiagramElement e) {
		Set<Dart> darts = getDartsForEdge(e);
		if (darts == null) {
			return null;
		} else {
			List<Vertex> out = new ArrayList<>(darts.size()+1);
			Vertex start = e.getFrom();
			Dart next = null;
			do {
				out.add(start);
				
				if (start == e.getTo()) {
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
	public List<Dart> getDartOrdering(Vertex around) {
		Dart up = getDartInDirection(around, Direction.UP);
		Dart right = getDartInDirection(around, Direction.RIGHT);
		Dart down = getDartInDirection(around, Direction.DOWN);
		Dart left = getDartInDirection(around, Direction.LEFT);
		List<Dart> out = new ArrayList<>(4);
		addIfNotNull(out, up);
		addIfNotNull(out, right);
		addIfNotNull(out, down);
		addIfNotNull(out, left);
		return out;
	}

	private void addIfNotNull(List<Dart> out, Dart up) {
		if (up != null) {
			out.add(up);
		}
	}
}
