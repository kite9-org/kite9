package org.kite9.diagram.visualization.orthogonalization.edge;

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

public class SimpleEdgeConverter implements EdgeConverter {

	private int newVertexId =0;

	public IncidentDart convertBiDirectionalEdge(BiDirectionalPlanarizationEdge e, Orthogonalization o, Direction incident, Vertex end, Vertex sideVertex) {
		Direction side = Direction.reverse(incident);
		ExternalVertex externalVertex = new ExternalVertex(end.getID()+"-ve"+newVertexId++, e);
		Dart d = o.createDart(sideVertex, externalVertex, e.getOriginalUnderlying(), side, null);
		return new IncidentDart(d, externalVertex, sideVertex, side, e);
	}

}
