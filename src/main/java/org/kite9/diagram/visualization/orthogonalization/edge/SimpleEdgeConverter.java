package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.Collections;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.framework.common.Kite9ProcessingException;

public class SimpleEdgeConverter implements EdgeConverter {
	
	ContentsConverter cc;

	public SimpleEdgeConverter(ContentsConverter cc) {
		this.cc = cc;
	}

	private int newVertexId =0;

	public IncidentDart convertPlanarizationEdge(PlanarizationEdge e, Orthogonalization o, Direction incident, Vertex end, Vertex sideVertex) {
		Direction side = Direction.reverse(incident);
		ExternalVertex externalVertex = createExternalvertex(e, end);
		o.createDart(sideVertex, externalVertex, createMap(e), side);
		return new IncidentDart(externalVertex, sideVertex, side, e);
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

	public ExternalVertex createExternalvertex(PlanarizationEdge e, Vertex end) {
		ExternalVertex externalVertex = new ExternalVertex(end.getID()+"-ve"+newVertexId++, e);
		return externalVertex;
	}

}
