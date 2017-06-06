package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.Collections;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.framework.common.Kite9ProcessingException;

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
	public void convertContainerEdge(Map<DiagramElement, Direction> underlyings, Orthogonalization o, Vertex end1, Vertex end2, Direction d, Side s) {
		Dart dart = o.createDart(end1, end2, underlyings, d);
		s.newEdgeDarts.add(dart);
	}

	protected Map<DiagramElement, Direction> createMap(PlanarizationEdge e) {
		if (e instanceof BorderEdge) {
			return e.getDiagramElements();
		} else if (e instanceof BiDirectionalPlanarizationEdge) {
			return Collections.singletonMap(((BiDirectionalPlanarizationEdge) e).getOriginalUnderlying(), null);
		} else {
			throw new Kite9ProcessingException();
		}
	}

}