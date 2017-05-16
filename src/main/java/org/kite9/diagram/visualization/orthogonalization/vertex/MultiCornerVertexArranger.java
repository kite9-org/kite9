package org.kite9.diagram.visualization.orthogonalization.vertex;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.DartJunctionVertex;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering;
import org.kite9.framework.common.Kite9ProcessingException;

/**
 * Creates darts for edges arriving at corners or sides of a planarization.
 * 
 * It is expected that multiple edges could arrive to a single point in the same direction.  This should split those out into 
 * some side-edge arrangement.
 * 
 * @author robmoffat
 *
 */
public class MultiCornerVertexArranger extends ConnectedVertexArranger {
	
	public MultiCornerVertexArranger(ElementMapper em) {
		super(em);
	}

	@Override
	protected void convertVertex(Orthogonalization o, Vertex v, TurnInformation ti) {
		if (v instanceof MultiCornerVertex) {
			List<IncidentDart> dartOrdering = createIncidentDartOrdering((MultiCornerVertex) v, o, ti);
			createBorderEdges(dartOrdering, o, (MultiCornerVertex) v);
			setupBoundariesFromIncidentDarts(dartOrdering, v);
		} else {
			super.convertVertex(o, v, ti);
		}
	}
	
	

	private void createBorderEdges(List<IncidentDart> dartDirections, Orthogonalization o, MultiCornerVertex v) {
		// create sides for any sides that have > 1 incident darts
		OPair<IncidentDart> borders = identifyBorders(dartDirections);
		Direction inDirection = borders.getA().d.getDrawDirectionFrom(borders.getA().external);
		Direction outDirection = Direction.reverse(borders.getB().d.getDrawDirectionFrom(borders.getB().external));
		
		if (inDirection == outDirection) {
			// single side
			
			Set<DiagramElement> aUnderlyings = borders.getA().dueTo.getDiagramElements().keySet();
			Set<DiagramElement> bUnderlyings = borders.getB().dueTo.getDiagramElements().keySet();
			if (!aUnderlyings.equals(bUnderlyings)) {
				throw new Kite9ProcessingException();
			}

			// work out which IncidentDarts to join up to the borders.
			Map<Direction, List<IncidentDart>> map =getDartsInDirection(dartDirections, v);
			if (map.size()==4) {
				throw new Kite9ProcessingException("Should only have darts leaving side");
			}
			Direction sideDirection = Direction.rotateClockwise(inDirection);
			List<IncidentDart> dartsToUse = map.get(sideDirection);
			if (dartsToUse == null) {
				sideDirection = Direction.reverse(sideDirection);
				dartsToUse = map.get(Direction.reverse(sideDirection));
			}
			
			createSide(borders.getA().internal, borders.getB().internal, aUnderlyings, dartsToUse, o, inDirection, sideDirection);
		} else {
			// two sides
			Map<Direction, List<IncidentDart>> map =getDartsInDirection(dartDirections, v);
			Vertex midPoint = new DartJunctionVertex("mcv-"+newVertexId++, v.getDiagramElements());
			
			Set<DiagramElement> side1Underlyings = borders.getA().dueTo.getDiagramElements().keySet();
			Set<DiagramElement> side2Underlyings = borders.getB().dueTo.getDiagramElements().keySet();

			List<IncidentDart> side1Darts = map.get(Direction.reverse(outDirection));
			List<IncidentDart> side2Darts = map.get(inDirection);
			
			
			createSide(borders.getA().internal, midPoint, side1Underlyings, side1Darts, o, inDirection, Direction.reverse(outDirection));
			createSide(midPoint, borders.getB().internal, side2Underlyings, side2Darts, o, outDirection, inDirection);
		}
	}

	private OPair<IncidentDart> identifyBorders(List<IncidentDart> dartDirections) {
		// there should be two IncidentDarts next to each other that represent the edge of the container
		List<IncidentDart> borders = dartDirections.stream().filter(d -> d.dueTo instanceof BorderEdge).collect(Collectors.toList());
		
		if (borders.size() != 2) {
			throw new Kite9ProcessingException();
		}
		
		IncidentDart to = borders.get(0);
		IncidentDart from = borders.get(1);

		int idx1 = dartDirections.indexOf(to);
		int idx2 = dartDirections.indexOf(from);
		
		if (idx2 - idx1 == 1) {
			return new OPair<IncidentDart>(from, to);
		} else if ((idx1 == 0) && (idx2 == dartDirections.size()-1)) {
			return new OPair<IncidentDart>(to, from);
		} else {
			throw new Kite9ProcessingException();
		}
	}

	@Override
	protected List<IncidentDart> createIncidentDartOrdering(Vertex around, Orthogonalization o, TurnInformation ti) {
		if (around instanceof MultiCornerVertex) {
			EdgeOrdering eo = o.getPlanarization().getEdgeOrderings().get(around);
			Set<DiagramElement> unds = ((MultiCornerVertex) around).getDiagramElements();
			
			int[] number = { 0 };
			
			List<IncidentDart> out = eo.getEdgesAsList().stream().map(
				e -> convertEdgeToIncidentDart(e, unds, o, Direction.reverse(ti.getIncidentDartDirection(e)), number[0]++, around)
				).collect(Collectors.toList());
		
			return out;
			
			
		} else {
			return super.createIncidentDartOrdering(around, o, ti);
		}
	}

	@Override
	protected IncidentDart convertEdgeToIncidentDart(PlanarizationEdge e, Set<DiagramElement> cd, Orthogonalization o, Direction leavingDirection, int i, Vertex und1) {
		if (e instanceof BorderEdge) {
			Map<DiagramElement, Direction> cn = ((BorderEdge)e).getDiagramElements();
			Set<DiagramElement> und = new HashSet<>();
			und.addAll(cn.keySet());
			und.addAll(cd);
			Vertex sideVertex = new DartJunctionVertex(und1.getID()+"-va-"+newVertexId++, und);
			Vertex externalVertex = createExternalVertex(sideVertex.getID()+"-e", (PlanarizationEdge) e);
			Dart d = o.createDart(sideVertex, externalVertex, cn, leavingDirection);
			return new IncidentDart(d, externalVertex, sideVertex, leavingDirection, e);
			
		} else {
			return super.convertEdgeToIncidentDart(e, cd, o, leavingDirection, i, und1);
		}
	}
	
	
}
