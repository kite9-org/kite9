package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

/**
 * Converts edges into darts.  In subclasses, takes account of fanning and labelling too.
 * @author robmoffat
 *
 */
public interface EdgeConverter {

	/**
	 * This is used for converting planarization edges (i.e. elements in the planarization) into darts.  
	 * 
	 */
	IncidentDart convertPlanarizationEdge(PlanarizationEdge e, Orthogonalization o, Direction incident, Vertex externalVertex, Vertex sideVertex, Vertex planVertex, Direction fanStep);
	
	/**
	 * This is used for creating darts to represent the 2d shape of a vertex, which was a point in the planarization.
	 */
	List<Dart> buildDartsBetweenVertices(Map<DiagramElement, Direction> underlyings, Orthogonalization o, Vertex end1, Vertex end2, Direction d);
	
}
