package org.kite9.diagram.visualization.orthogonalization.vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.Edge;
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
import org.kite9.diagram.visualization.orthogonalization.edge.ContainerLabelConverter;
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter;
import org.kite9.diagram.visualization.orthogonalization.edge.IncidentDart;
import org.kite9.diagram.visualization.orthogonalization.edge.LabellingEdgeConverter;
import org.kite9.diagram.visualization.orthogonalization.edge.Side;
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
	
	protected EdgeConverter ec;
	
	protected ContainerLabelConverter clc;
	
	public ContainerLabelConverter getContainerLabelConverter() {
		return clc;
	}

	public ConnectedVertexArranger(ElementMapper em) {
		super();
		this.gp = em.getGridPositioner();
		this.em = em;
		LabellingEdgeConverter labellingEdgeConverter = new LabellingEdgeConverter(this, em);
		this.ec = labellingEdgeConverter;
		this.clc = labellingEdgeConverter;
	}

	public static final int INTER_EDGE_SEPARATION = 0;

	protected int newVertexId;

	/**
	 * This implementation handles ConnectedVertex implementations embedded in the planarization.
	 */
	protected DartFace convertVertex(Orthogonalization o, Vertex v, TurnInformation ti) {
		if (!(v instanceof ConnectedVertex)) 
			throw new Kite9ProcessingException();
	
		Map<Direction, List<IncidentDart>> dartDirections = getDartsInDirection(v, o, ti);
		Connected originalUnderlying = ((ConnectedVertex) v).getOriginalUnderlying();
		DartFace out = convertDiagramElementToInnerFace(originalUnderlying, v, o, dartDirections);
		List<IncidentDart> allIncidentDarts = convertToDartList(dartDirections);
		setupBoundariesFromIncidentDarts(allIncidentDarts, v);
		return out;
	}
	
	protected List<IncidentDart> convertToDartList(Map<Direction, List<IncidentDart>> dartDirections) {
		List<IncidentDart> out = new ArrayList<>();
		out.addAll(dartDirections.get(Direction.UP));
		out.addAll(dartDirections.get(Direction.RIGHT));
		out.addAll(dartDirections.get(Direction.DOWN));
		out.addAll(dartDirections.get(Direction.LEFT));
		return out;
	}
	
	@Override
	public boolean needsConversion(Vertex v) {
		return (v instanceof ConnectedVertex);
	}

	@Override
	public List<DartDirection> returnAllDarts(Vertex v, Orthogonalization o) {
		if (!(v instanceof ConnectedVertex)) 
			throw new Kite9ProcessingException();
		
		Connected c = ((ConnectedVertex) v).getOriginalUnderlying();
		List<DartFace> faces = o.getDartFacesForRectangular((Rectangular) c);
		
		if (faces.size() == 1) {
			return faces.get(0).getDartsInFace();
		} else if (faces.size() > 1) {
			throw new Kite9ProcessingException();
		} else {
			DartFace out = convertVertex(o, v, new TurnInformation() {
				
				@Override
				public Direction getIncidentDartDirection(Edge e) {
					return null;
				}
			});
			
			return out.getDartsInFace();
		}
	}
	
	
	protected void setupBoundariesFromIncidentDarts(List<IncidentDart> dartOrdering, Vertex v) {
		Set<Vertex> externalVertices = dartOrdering.stream().map(id -> id.getExternal()).collect(Collectors.toSet());
		setupBoundaries(externalVertices, v);
	}
	
	/**
	 * Creates a dart or darts that arrives at the vertex.  Where more than one dart is created, return just the dart hitting
	 * the vertex.
	 * @param und 
	 * @param count 
	 */
	protected IncidentDart convertEdgeToIncidentDart(PlanarizationEdge e, Set<DiagramElement> cd, Orthogonalization o, Direction incident, int idx, Vertex und, int count) {
		Vertex sideVertex = createSideVertex(cd, und);
		return ec.convertPlanarizationEdge(e, o, incident, und, sideVertex);
	}

	protected Vertex createSideVertex(Set<DiagramElement> cd, Vertex und) {
		Set<DiagramElement> underlyings = new HashSet<>();
		underlyings.addAll(cd);
		underlyings.addAll(und.getDiagramElements());
		
		Vertex sideVertex = new DartJunctionVertex(und.getID()+"-dv-"+newVertexId++, underlyings);
		return sideVertex;
	}
	
	public DartFace convertDiagramElementToInnerFace(DiagramElement original, Orthogonalization o) {
		return convertDiagramElementToInnerFace(original, null, o, new HashMap<>());
	}

	protected DartFace convertDiagramElementToInnerFace(DiagramElement originalUnderlying, Vertex optionalExistingVertex, Orthogonalization o, Map<Direction, List<IncidentDart>> dartDirections) {
		log.send(log.go() ? null : "Converting: " + originalUnderlying + " with edges: ", dartDirections);
		
		CornerVertices cv = em.getOuterCornerVertices(originalUnderlying);

		List<MultiCornerVertex> perimeter = gp.getClockwiseOrderedContainerVertices(cv);
		return convertDiagramElementToInnerFaceWithCorners(originalUnderlying, o, dartDirections, perimeter);
	}

	protected DartFace convertDiagramElementToInnerFaceWithCorners(DiagramElement originalUnderlying, Orthogonalization o, Map<Direction, List<IncidentDart>> dartDirections, List<MultiCornerVertex> perimeter) {
		LinkedHashSet<Dart> allSideDarts = new LinkedHashSet<Dart>();
		Direction sideDirection = Direction.RIGHT;  // initial direction of perimeter
		MultiCornerVertex start = perimeter.get(0);
		int done = 0;
		while (done < perimeter.size()) {
			MultiCornerVertex fromv = perimeter.get(done);
			MultiCornerVertex tov = perimeter.get((done+1) % perimeter.size());
			Direction outwardsDirection = Direction.rotateAntiClockwise(sideDirection);
			List<IncidentDart> leavers = dartDirections.get(outwardsDirection);
			leavers = leavers == null ? Collections.emptyList() : leavers;
			Side s = createSide(fromv, tov, originalUnderlying, leavers, o, sideDirection, outwardsDirection);
			allSideDarts.addAll(s.getDarts());
			done ++;
			sideDirection = Direction.rotateClockwise(sideDirection);
		}

		DartFace inner = createInnerFace(o, allSideDarts, start, originalUnderlying);
		clc.handleContainerLabels(inner, originalUnderlying, o);
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
	
	@Override
	public DartFace convertGridToOuterFace(Orthogonalization o, Vertex startVertex, Rectangular partOf) {
		throw new Kite9ProcessingException("Not implemented");
	}

	protected DartFace createInnerFace(Orthogonalization o, LinkedHashSet<Dart> allSideDarts, Vertex start, DiagramElement de) {
		List<DartDirection>  dd = dartsToDartFace(allSideDarts, start, false);
		DartFace inner = o.createDartFace((Rectangular) de, false, dd);
		return inner;
	}
	
	private List<DartDirection> dartsToDartFace(Set<Dart> allDarts, Vertex vs, boolean reverse) {
		List<DartDirection> dartsInFace = new ArrayList<DartDirection>(allDarts.size());
		
		for (Dart dart : allDarts) {
			Direction d = dart.getDrawDirectionFrom(vs);
			d = reverse ? Direction.reverse(d) : d;
			dartsInFace.add(new DartDirection(dart, d));
			vs = dart.otherEnd(vs);
		}
		
		if (reverse) {
			Collections.reverse(dartsInFace);
		}
		
		return dartsInFace;
	}

	/**
	 * Note - we can't just group otherwise order gets broken.  We must ensure the sides are still in order
	 */
	protected Map<Direction, List<IncidentDart>> getDartsInDirection(Vertex from, Orthogonalization o, TurnInformation ti) {
		EdgeOrdering eo = o.getPlanarization().getEdgeOrderings().get(from);
		Set<DiagramElement> cd = from.getDiagramElements();
		
		List<PlanarizationEdge> processOrder = eo.getEdgesAsList();
		// find the first edge
		int startPoint = 0;
		for (int i = 0; i < eo.size(); i++) {
			PlanarizationEdge current = processOrder.get(i);
			PlanarizationEdge prev = processOrder.get((i - 1 + processOrder.size()) % processOrder.size());
			
			if (ti.getIncidentDartDirection(prev) != ti.getIncidentDartDirection(current)) {
				startPoint = i;
				break;
			}
		}
		
		Map<Direction, List<PlanarizationEdge>> in = new HashMap<>();
		in.put(Direction.UP, new ArrayList<>());
		in.put(Direction.DOWN, new ArrayList<>());
		in.put(Direction.LEFT, new ArrayList<>());
		in.put(Direction.RIGHT, new ArrayList<>());
		
		for (int i = 0; i < processOrder.size(); i++) {
			PlanarizationEdge planarizationEdge = processOrder.get((i + startPoint ) % processOrder.size());
			in.get(Direction.reverse(ti.getIncidentDartDirection(planarizationEdge))).add(planarizationEdge);
		}
		
		Map<Direction, List<IncidentDart>> out = new HashMap<>();
		
		for (Direction dir : in.keySet()) {
			List<PlanarizationEdge> list = in.get(dir);
			int count = list.size();
			int[] number = { 0 };
			out.put(dir, list.stream().map(
				e -> convertEdgeToIncidentDart(e, cd, o, ti.getIncidentDartDirection(e), number[0]++, from, count)).collect(Collectors.toList()));
		
		}
		
		return out;
	}

	/**
	 * Links corner elements with the incident darts into the whole side of the underlying.
	 */
	protected Side createSide(Vertex from, Vertex to, DiagramElement underlying, List<IncidentDart> onSide, Orthogonalization o, Direction goingIn, Direction side) {
		Side out = new Side();
		Vertex last = from;
		IncidentDart incidentDart = null;
		
		if (onSide != null) {
			for (int j = 0; j < onSide.size(); j++) {
				incidentDart = onSide.get(j);
				Vertex vsv = incidentDart.getInternal();
				ec.convertContainerEdge(underlying, o, last, vsv, side, goingIn, out);
				out.addVertex(vsv);			
				last = vsv;
			}
		}

		// finally, join to corner
		ec.convertContainerEdge(underlying, o, last, to, side, goingIn, out);
		return out;
	}

	public String getPrefix() {
		return "FACE";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

	
}
