package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.HashSet;
import java.util.Set;

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.DartJunctionVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

public class SimpleEdgeConverter implements EdgeConverter {

	private int newVertexId =0;

	public IncidentDart convertBiDirectionalEdge(BiDirectionalPlanarizationEdge e, Set<DiagramElement> cd, Orthogonalization o, Direction incident, Vertex und, DiagramElement cn) {
		Direction side = Direction.reverse(incident);
		
		Set<DiagramElement> underlyings = new HashSet<>();
		underlyings.addAll(cd);
		underlyings.add(cn);
		
		Vertex sideVertex = new DartJunctionVertex(und.getID()+"-dv-"+newVertexId++, underlyings);
		Vertex externalVertex = createExternalVertex(sideVertex.getID()+"-e", (PlanarizationEdge) e);
		Dart d = o.createDart(sideVertex, externalVertex, cn, side, null);
		return new IncidentDart(d, externalVertex, sideVertex, side, e);
	}
	

	public Vertex createExternalVertex(String id, PlanarizationEdge pe) {
		return new ExternalVertex(id, pe);
	}
	

}
