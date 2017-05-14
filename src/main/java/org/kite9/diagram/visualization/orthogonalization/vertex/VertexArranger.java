package org.kite9.diagram.visualization.orthogonalization.vertex;

import java.util.List;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

/**
 * The vertex arranger is part of the Orthogonalization process
 * where a vertex with dimensionality is converted into a number of darts
 * 
 * @author robmoffat
 *
 */
public interface VertexArranger {
	
	public interface TurnInformation {
		
		/**
		 * Direction of dart arriving at this vertex, after orthogonalization.
		 */
		public Direction getIncidentDartDirection(Edge e);
		
	}

	/**
	 * Returns a subset of edges around the Rectangular perimeter which take you from the end of the incoming 
	 * connection edge to the start of the outgoing connection edge.
	 */
	public List<DartDirection> returnDartsBetween(PlanarizationEdge in, Direction outDirection, Vertex v, PlanarizationEdge out, Orthogonalization o, TurnInformation ti);
		
}
