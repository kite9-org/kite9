package org.kite9.diagram.visualization.orthogonalization.vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.grid.GridPositioner;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.ConnectedVertex;
import org.kite9.diagram.common.elements.vertex.DartJunctionVertex;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Logable;

/**
 * This converts a ConnectedVertex to a face, and darts, which can then be added to the Orthogonalization.
 * 
 * @author robmoffat
 */
public class ConnectedVertexArranger extends AbstractVertexArranger implements Logable, VertexArranger {
	
	protected GridPositioner gp;
	
	protected ElementMapper em;
	
	public ConnectedVertexArranger(ElementMapper em) {
		super();
		this.gp = em.getGridPositioner();
		this.em = em;
	}

	public static final int INTER_EDGE_SEPARATION = 0;

	/**
	 * Holds vertices and darts for one side of the vertex being converted
	 */
	static class Side {

		List<Vertex> vertices = new ArrayList<Vertex>();

		List<Dart> newEdgeDarts = new ArrayList<Dart>();

	}
	
	/**
	 * Holds info about the darts due to an incoming edge.
	 */
	static class IncidentDart {
		
		final Dart d;
		final Vertex external;
		final Vertex internal;
		final Direction arrivalSide;
		final PlanarizationEdge dueTo;
		
		public IncidentDart(Dart d, Vertex external, Vertex internal, Direction arrivalSide, PlanarizationEdge e) {
			super();
			this.d = d;
			this.external = external;
			this.internal = internal;
			this.arrivalSide = arrivalSide;
			this.dueTo = e;
		}
		
	}
	
	protected int newVertexId;

	/**
	 * This implementation handles ConnectedVertex implementations embedded in the planarization.
	 */
	protected void convertVertex(Orthogonalization o, Vertex v, TurnInformation ti) {
		if (!(v instanceof ConnectedVertex)) 
			return;
	
		List<IncidentDart> dartOrdering = createIncidentDartOrdering((ConnectedVertex) v, o, ti);
		Map<Direction, List<IncidentDart>> dartDirections = getDartsInDirection(dartOrdering, v);
		Connected originalUnderlying = ((ConnectedVertex) v).getOriginalUnderlying();
		convertDiagramElementToInnerFace(originalUnderlying, v, o, dartDirections);
		setupBoundariesFromIncidentDarts(dartOrdering, v);
	}

	protected void setupBoundariesFromIncidentDarts(List<IncidentDart> dartOrdering, Vertex v) {
		Set<Vertex> externalVertices = dartOrdering.stream().map(id -> id.external).collect(Collectors.toSet());
		setupBoundaries(externalVertices, v);
	}
	
	protected List<IncidentDart> createIncidentDartOrdering(Vertex around, Orthogonalization o, TurnInformation ti) {
		if (around instanceof ConnectedVertex) {
			EdgeOrdering eo = o.getPlanarization().getEdgeOrderings().get(around);
			Set<DiagramElement> cd = around.getDiagramElements();
			
			int[] number = { 0 };
			
			List<IncidentDart> out = eo.getEdgesAsList().stream().map(
				e -> convertEdgeToIncidentDart(e, cd, o, ti.getIncidentDartDirection(e), number[0]++, around)
				).collect(Collectors.toList());
		
			return out;
		} else {
			throw new Kite9ProcessingException();
		}
	}
	
	/**
	 * Creates a dart or darts that arrives at the vertex.  Where more than one dart is created, return just the dart hitting
	 * the vertex.
	 * @param und 
	 */
	protected IncidentDart convertEdgeToIncidentDart(PlanarizationEdge e, Set<DiagramElement> cd, Orthogonalization o, Direction incident, int i, Vertex und) {
		DiagramElement cn = null;
		if (e instanceof BiDirectionalPlanarizationEdge) {
			cn = ((BiDirectionalPlanarizationEdge) e).getOriginalUnderlying();
		} else {
			throw new Kite9ProcessingException();
		}
		
		Direction side = Direction.reverse(incident);
		
		Set<DiagramElement> underlyings = new HashSet<>();
		underlyings.addAll(cd);
		underlyings.add(cn);
		
		Vertex sideVertex = new DartJunctionVertex(und.getID()+"-dv-"+newVertexId++, underlyings);
		Vertex externalVertex = createExternalVertex(sideVertex.getID()+"-e", (PlanarizationEdge) e);
		Dart d = o.createDart(sideVertex, externalVertex, cn, side, null);
		return new IncidentDart(d, externalVertex, sideVertex, side, e);
	}

	protected DartFace convertDiagramElementToInnerFace(DiagramElement originalUnderlying, Vertex optionalExistingVertex, Orthogonalization o, Map<Direction, List<IncidentDart>> dartDirections) {
		log.send(log.go() ? null : "Converting: " + originalUnderlying + " with edges: ", dartDirections);
		
		CornerVertices cv = em.getOuterCornerVertices(originalUnderlying);

		LinkedHashSet<Dart> allSideDarts = new LinkedHashSet<Dart>();

		List<MultiCornerVertex> perimeter = gp.getClockwiseOrderedContainerVertices(cv);
		return convertDiagramElementToInnerFaceWithCorners(originalUnderlying, o, dartDirections, allSideDarts, perimeter);
	}

	protected DartFace convertDiagramElementToInnerFaceWithCorners(DiagramElement originalUnderlying, Orthogonalization o, Map<Direction, List<IncidentDart>> dartDirections, LinkedHashSet<Dart> allSideDarts,
			List<MultiCornerVertex> perimeter) {
		Direction sideDirection = Direction.RIGHT;  // initial direction of perimeter
		MultiCornerVertex start = perimeter.get(0);
		int done = 0;
		while (done < perimeter.size()) {
			MultiCornerVertex fromv = perimeter.get(done);
			MultiCornerVertex tov = perimeter.get((done+1) % perimeter.size());
			Direction outwardsDirection = Direction.rotateAntiClockwise(sideDirection);
			List<IncidentDart> leavers = dartDirections.get(outwardsDirection);
			Side s = createSide(fromv, tov, Collections.singleton(originalUnderlying), leavers, o, sideDirection, outwardsDirection);
			allSideDarts.addAll(s.newEdgeDarts);
			done ++;
			sideDirection = Direction.rotateClockwise(sideDirection);
			
		}

		DartFace inner = createInnerFace(o, allSideDarts, start, originalUnderlying);
		
		log.send("Created face: "+inner);
		
		// convert content
		if (originalUnderlying instanceof Container) {
			convertContainerContents(o, (Container) originalUnderlying, inner); 
		}
		
		return inner;
	}
	
	protected void convertContainerContents(Orthogonalization o, Container originalUnderlying, DartFace inner) {
		// does nothing in this implementation - see ContainerContentsArranger
	}

	protected DartFace createInnerFace(Orthogonalization o, LinkedHashSet<Dart> allSideDarts, Vertex start, DiagramElement de) {
		DartFace inner = o.createDartFace((Rectangular) de, false);
		dartsToDartFace(allSideDarts, start, inner, false);
		return inner;
	}
	

	
	private void dartsToDartFace(Set<Dart> allDarts, Vertex vs, DartFace df, boolean reverse) {
		df.dartsInFace = new ArrayList<DartDirection>(allDarts.size());
		
		for (Dart dart : allDarts) {
			Direction d = dart.getDrawDirectionFrom(vs);
			d = reverse ? Direction.reverse(d) : d;
			df.dartsInFace.add(new DartDirection(dart, d));
			vs = dart.otherEnd(vs);
		}
		
		if (reverse) {
			Collections.reverse(df.dartsInFace);
		}
	}

	

	

	protected Map<Direction, List<IncidentDart>> getDartsInDirection(List<IncidentDart> processOrder, Vertex from) {
		return processOrder.stream().collect(Collectors.groupingBy(id -> id.arrivalSide));
	}

	/**
	 * Links corner elements with the incident darts into the whole side of the underlying.
	 */
	protected Side createSide(Vertex from, Vertex to, Set<DiagramElement> underlying, List<IncidentDart> onSide, Orthogonalization o, Direction goingIn, Direction side) {
		Side out = new Side();
		Vertex last = from;
		IncidentDart incidentDart = null;
		
		if (onSide != null) {
			for (int j = 0; j < onSide.size(); j++) {
				incidentDart = onSide.get(j);
				Vertex vsv = incidentDart.internal;
				Dart sideDart = o.createDart(last, vsv, underlying, goingIn, side); 
				sideDart.setOrthogonalPositionPreference(goingIn);
				out.newEdgeDarts.add(sideDart);
				out.vertices.add(vsv);			
				last = vsv;
			}
		}

		// finally, join to corner
		Dart sideDart = o.createDart(last, to, underlying, goingIn, side); 
		sideDart.setOrthogonalPositionPreference(goingIn);
		out.newEdgeDarts.add(sideDart);
		return out;
	}

	public String getPrefix() {
		return "FACE";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
