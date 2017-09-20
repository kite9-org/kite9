package org.kite9.diagram.visualization.planarization.ordering;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.model.position.Direction;

/**
 * Defines the ordering of edges around a vertex and provides methods for changing the order.
 * 
 * @author robmoffat
 *
 */
public interface VertexEdgeOrdering extends EdgeOrdering {

	public void addEdgeDirection(Direction d, boolean isContradicting);

	public void remove(PlanarizationEdge toRemove);

	public void replace(PlanarizationEdge b, PlanarizationEdge a);
	
}
