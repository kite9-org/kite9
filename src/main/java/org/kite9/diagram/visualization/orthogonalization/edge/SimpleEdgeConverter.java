package org.kite9.diagram.visualization.orthogonalization.edge;

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter;

public class SimpleEdgeConverter implements EdgeConverter {
	
	ContentsConverter cc;

	public SimpleEdgeConverter(ContentsConverter cc) {
		this.cc = cc;
	}

	private int newVertexId =0;

	public IncidentDart convertBiDirectionalEdge(BiDirectionalPlanarizationEdge e, Orthogonalization o, Direction incident, Vertex end, Vertex sideVertex) {
		Direction side = Direction.reverse(incident);
		ExternalVertex externalVertex = createExternalvertex(e, end);
		o.createDart(sideVertex, externalVertex, e.getOriginalUnderlying(), side, null);
		return new IncidentDart(externalVertex, sideVertex, side, e);
	}

	public ExternalVertex createExternalvertex(BiDirectionalPlanarizationEdge e, Vertex end) {
		ExternalVertex externalVertex = new ExternalVertex(end.getID()+"-ve"+newVertexId++, e);
		return externalVertex;
	}

}
