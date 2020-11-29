package org.kite9.diagram.visualization.orthogonalization.vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;
import org.kite9.diagram.logging.LogicException;

/**
 * This mainly handles returning DartDirection objects which form the Boundary of a vertex in the diagram.
 * Actual conversion from Planarization vertices to darts is handled by subclasses.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractVertexArranger implements VertexArranger, Logable, ContentsConverter {
	
	protected ElementMapper em;
	
	protected Kite9Log log = new Kite9Log(this);

	public AbstractVertexArranger(ElementMapper em) {
		super();
		this.em = em;
	}
	
	/**
	 * Contains part of the overall vertex construction, between one incoming vertex and the next.
	 * @author robmoffat
	 *
	 */
	private static class Boundary {
		
		public Boundary(ExternalVertex from, ExternalVertex to, List<DartDirection> toInsert) {
			super();
			this.from = from;
			this.to = to;
			this.toInsert = toInsert;
		}

		final ExternalVertex from, to;
		final List<DartDirection> toInsert;
		
		public String toString() {
			return "Boundary [from=" + from + ", to=" + to + "]";
		}
	}
	
	private Map<Vertex, List<Boundary>> boundaries = new HashMap<>();

	@Override
	public List<DartDirection> returnDartsBetween(PlanarizationEdge in, Direction outDirection, Vertex v, PlanarizationEdge out, Orthogonalization o, TurnInformation ti) {
		List<Boundary> relevantBoundaries = boundaries.get(v);
		if (relevantBoundaries == null) {
			convertVertex(o, v, ti);
			relevantBoundaries = boundaries.get(v); 
		}
		
		return findDartsToInsert(relevantBoundaries, in, outDirection, out);
	}

	protected abstract DartFace convertVertex(Orthogonalization o, Vertex v, TurnInformation ti);

	/**
	 * Works out which darts are needed from the vertex to fill a gap between in
	 * and out in the face. Always proceeds in an anti-clockwise direction.
	 * 
	 * @param outerFace
	 */
	private List<DartDirection> findDartsToInsert(List<Boundary> relevantBoundaries, PlanarizationEdge in, Direction outDirection, PlanarizationEdge out) {
		for (Boundary b : relevantBoundaries) {
			if ((b.from.joins(in)) && (b.to.joins(out))) {
//				if ((b.getEndDirection() == outDirection)) {
					return b.toInsert;
//				}
			}

		}

		throw new LogicException();
	}
	
	private static int antiClockwiseTurns(Direction from, Direction to) {
		int c = 0;
		while (from != to) {
			from = Direction.rotateAntiClockwise(from);
			c++;
		}
		
		return c;
	}
	
	public static Dart getNextDartAntiClockwise(Vertex incidentVertex, Dart in) {
		Direction directionToVertex = Direction.reverse(in.getDrawDirectionFrom(incidentVertex));
		Dart out = null;
		int bestScore = 100; 
		
		for (Edge e : incidentVertex.getEdges()) {
			if (e instanceof Dart) {
				Direction thisDirection = Direction.reverse(e.getDrawDirectionFrom(incidentVertex));
				int turns = antiClockwiseTurns(directionToVertex, thisDirection);
				if ((turns > 0) && (turns<bestScore)) {
					out = (Dart) e;
					bestScore = turns;
				}
			}
		}
		
		if (out == null) {
			return in;
		}
		
		return out;
	}
	
	/**
	 * Returns the dart for an external vertex.  There should only be one.
	 */
	private Dart getSingleExternalVertexDart(Vertex from) {
		if (from.getEdgeCount() != 1) {
			throw new LogicException();
		} 
		
		return (Dart) from.getEdges().iterator().next();
	}

	
	
	/**
	 * Divides up the darts around the vertex between the darts entering the vertex.
	 * The arrangement of darts at this point should look something like a spider, with the ends of the legs
	 * being the external vertices.
	 * 
	 * Note:  because we are returning the vertex boundaries to include in other faces, we trace in an 
	 * anti-clockwise direction, as a face touching this boundary will proceed around it in an anti-clockwise
	 * direction.
	 */
	protected void setupBoundaries(Set<Vertex> externalVertices, Vertex forVertex) {
		List<Boundary> made = new ArrayList<Boundary>();
		Set<Vertex> toProcess = new HashSet<>(externalVertices);

		for (Vertex from : toProcess) {
			Dart dart = null;
			dart = getSingleExternalVertexDart(from);
			List<DartDirection> dds = new ArrayList<>();
			Vertex to = dart.otherEnd(from);
			Direction d = dart.getDrawDirectionFrom(from);
			
			do {
				dds.add(new DartDirection(dart, d));
				dart = getNextDartAntiClockwise(to, dart);
				d = dart.getDrawDirectionFrom(to);
				to = dart.otherEnd(to);
			} while (!externalVertices.contains(to));
			
			dds.add(new DartDirection(dart, d));
			Boundary b = new Boundary((ExternalVertex) from, (ExternalVertex) to, dds);
			made.add(b);
			from = to;
			
			log.send("Vertex: "+forVertex+" has boundary: "+b+" with darts: "+b.toInsert);
		}
		
		boundaries.put(forVertex, made);
	}

	private int newVertexId =0;
	
	public ExternalVertex createExternalVertex(PlanarizationEdge e, Vertex end) {
		ExternalVertex externalVertex = new ExternalVertex(end.getID()+"-ve"+newVertexId++, e);
		return externalVertex;
	}

	
}
