package org.kite9.diagram.visualization.planarization.ordering;

import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.position.Direction;

/**
 * Defines the ordering of edges around a vertex and provides methods for changing the order.
 * 
 * @author robmoffat
 *
 */
public interface VertexEdgeOrdering extends EdgeOrdering {

	public void addEdgeDirection(Direction d, boolean isContradicting);

	public void remove(Edge toRemove);

	public void replace(Edge b, Edge a);
	
}
