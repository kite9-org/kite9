package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

public interface EdgeConverter {

	IncidentDart convertPlanarizationEdge(PlanarizationEdge e, Orthogonalization o, Direction incident, Vertex externalVertex, Vertex sideVertex, Vertex planVertex, Direction fanStep);
	
	void convertContainerEdge(Map<DiagramElement, Direction> underlyings, Orthogonalization o, Vertex end1, Vertex end2, Direction d, List<Dart> s);

	void createEdgePart(Orthogonalization o, Direction direction, Vertex start, Vertex end, DiagramElement forDe, Direction forDeSide, List<DartDirection> out);

	
}
