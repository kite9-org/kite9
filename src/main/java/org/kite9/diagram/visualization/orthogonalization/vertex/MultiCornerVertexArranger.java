package org.kite9.diagram.visualization.orthogonalization.vertex;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.DartJunctionVertex;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.edge.IncidentDart;
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
	public boolean needsConversion(Vertex v) {
		if ((v instanceof MultiCornerVertex) && (v.getDiagramElements().size()==1)) {
			return true;
		} else {
			return super.needsConversion(v);
		}
	}
	
	@Override
	protected DartFace convertVertex(Orthogonalization o, Vertex v, TurnInformation ti) {
		if (v instanceof MultiCornerVertex) {
			List<IncidentDart> dartOrdering = createIncidentDartOrdering((MultiCornerVertex) v, o, ti);
			createBorderEdges(dartOrdering, o, (MultiCornerVertex) v);
			setupBoundariesFromIncidentDarts(dartOrdering, v);
			return null;
		} else {
			return super.convertVertex(o, v, ti);
		}
	}
	
	

	private void createBorderEdges(List<IncidentDart> dartDirections, Orthogonalization o, MultiCornerVertex v) {
		// create sides for any sides that have > 1 incident darts
		OPair<IncidentDart> borders = identifyBorders(dartDirections);
		Direction inDirection = Direction.reverse(borders.getA().getArrivalSide());
		Direction outDirection = borders.getB().getArrivalSide();
		
		if (inDirection == outDirection) {
			// single side
			Direction sideDirection = Direction.rotateClockwise(inDirection);
			
			DiagramElement aUnderlying = ((BorderEdge) borders.getA().getDueTo()).getElementForSide(sideDirection);
			DiagramElement bUnderlying = ((BorderEdge) borders.getB().getDueTo()).getElementForSide(sideDirection);
			
			if (aUnderlying != bUnderlying) {
				throw new Kite9ProcessingException();
			}

			// work out which IncidentDarts to join up to the borders.
			Map<Direction, List<IncidentDart>> map =getDartsInDirection(dartDirections, v);

			List<IncidentDart> dartsToUse = map.get(sideDirection);
			if (dartsToUse.size() == 0) {
				sideDirection = Direction.reverse(sideDirection);
				dartsToUse = map.get(sideDirection);
			}
			
			createSide(borders.getA().getInternal(), borders.getB().getInternal(), aUnderlying, dartsToUse, o, inDirection, sideDirection);
		} else {
			// two sides
			Map<Direction, List<IncidentDart>> map =getDartsInDirection(dartDirections, v);
			Vertex midPoint = new DartJunctionVertex("mcv-"+newVertexId++, v.getDiagramElements());
			
			Direction aSide = Direction.reverse(outDirection);
			Direction bSide = inDirection;
			DiagramElement aUnderlying = ((BorderEdge) borders.getA().getDueTo()).getElementForSide(aSide);
			DiagramElement bUnderlying = ((BorderEdge) borders.getB().getDueTo()).getElementForSide(bSide);

			if (aUnderlying != bUnderlying) {
				throw new Kite9ProcessingException();
			}

			List<IncidentDart> side1Darts = map.get(aSide);
			List<IncidentDart> side2Darts = map.get(inDirection);
			
			
			createSide(borders.getA().getInternal(), midPoint, aUnderlying, side1Darts, o, inDirection, aSide);
			createSide(midPoint, borders.getB().getInternal(), aUnderlying, side2Darts, o, outDirection, bSide);
		}
	}

	private OPair<IncidentDart> identifyBorders(List<IncidentDart> dartDirections) {
		// there should be two IncidentDarts next to each other that represent the edge of the container
		List<IncidentDart> borders = dartDirections.stream().filter(d -> d.getDueTo() instanceof BorderEdge).collect(Collectors.toList());
		
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
	
}
