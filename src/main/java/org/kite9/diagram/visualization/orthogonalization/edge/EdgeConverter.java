package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.Set;

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

/**
 * Knows how to convert ta
 * @author robmoffat
 *
 */
public interface EdgeConverter {

	IncidentDart convertBiDirectionalEdge(BiDirectionalPlanarizationEdge e, 
			Set<DiagramElement> cd, Orthogonalization o, Direction incident, Vertex und, DiagramElement cn);

}
