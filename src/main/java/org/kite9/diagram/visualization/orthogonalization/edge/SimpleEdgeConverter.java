package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.framework.logging.LogicException;

public class SimpleEdgeConverter implements EdgeConverter {
	
	ContentsConverter cc;

	public SimpleEdgeConverter(ContentsConverter cc) {
		this.cc = cc;
	}

	public IncidentDart convertPlanarizationEdge(PlanarizationEdge e, Orthogonalization o, Direction incident, Vertex externalVertex, Vertex sideVertex, Vertex planVertex, Direction fanStep) {
		Direction side = Direction.reverse(incident);
		o.createDart(sideVertex, externalVertex, createMap(e), side);
		return new IncidentDart(externalVertex, sideVertex, side, e);
	} 
	
	@Override
	public void convertContainerEdge(Map<DiagramElement, Direction> underlyings, Orthogonalization o, Vertex end1, Vertex end2, Direction d, List<Dart> s) {
		List<Dart> start = new ArrayList<Dart>();
		List<Dart> end = new ArrayList<Dart>();
		
		boolean cont = true;
		
		while (cont) {			
			Dart end1Leaver = getDartGoing(end1, d);
			Dart end2Leaver = getDartGoing(end2, Direction.reverse(d));
			
			cont = false;
			if (end1Leaver !=null) {
				// add underlyings
				start.add(o.createDart(end1Leaver.getFrom(), end1Leaver.getTo(), underlyings, end1Leaver.getDrawDirection()));
				cont = true;
				end1 = end1Leaver.otherEnd(end1);
			}
			
			if ((end2Leaver !=null) && (end2Leaver != end1Leaver)) {
				// add underlyings
				end.add(o.createDart(end2Leaver.getFrom(), end2Leaver.getTo(), underlyings, end2Leaver.getDrawDirection()));
				cont = true;
				end2 = end2Leaver.otherEnd(end2);
			}
			
			if (end1==end2) {
				cont = false;
			}
		}
		
		if (end1 != end2) {
			start.add(o.createDart(end1, end2, underlyings, d));
		}
		
		Collections.reverse(end);
		start.addAll(end);
		s.addAll(start);
	}

	private Dart getDartGoing(Vertex end1, Direction d) {
		for (Edge e : end1.getEdges()) {
			if ((e instanceof Dart) && (((Dart)e).getDrawDirectionFrom(end1) == d)) {
				return (Dart) e;
			}
		}
		
		return null;
	}

	protected Map<DiagramElement, Direction> createMap(PlanarizationEdge e) {
		if (e instanceof BorderEdge) {
			return e.getDiagramElements();
		} else if (e instanceof BiDirectionalPlanarizationEdge) {
			return Collections.singletonMap(((BiDirectionalPlanarizationEdge) e).getOriginalUnderlying(), null);
		} else {
			throw new LogicException();
		}
	}

	@Override
	public void createEdgePart(Orthogonalization o, Direction direction, Vertex start, Vertex end, DiagramElement forDe, Direction forDeSide, List<DartDirection> out) {
		Dart dart = o.createDart(start, end, forDe, direction, forDeSide);
		DartDirection dd = new DartDirection(dart, direction);
		out.add(dd);
	}
}