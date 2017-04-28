package org.kite9.diagram.common.elements.vertex;

import java.util.Set;

import org.kite9.diagram.common.elements.ArtificialElement;
import org.kite9.diagram.common.elements.Positioned;
import org.kite9.diagram.common.elements.Routable;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.model.DiagramElement;

/**
 * Represents any positionable shape in the diagram, for topological 
 * arrangers that don't have to care about what they are arranging.
 * 
 * @author robmoffat
 *
 * @param <E>
 */
public interface Vertex extends Comparable<Vertex>, ArtificialElement, Positioned, Routable {
	
	/**
	 * User identifier for the vertex.  
	 */
	public String getID();
	
	/**
	 * True if this vertex is connected by an edge to v.
	 */
	public boolean isLinkedDirectlyTo(Vertex v);
	
	/**
	 * Return true if the vertex has length and breadth.  False if it is a point vertex.  
	 */
	public boolean hasDimension();

	public int getEdgeCount();

	public Iterable<Edge> getEdges();

	public void removeEdge(Edge e);

	public void addEdge(Edge e);
	
	public Set<DiagramElement> getDiagramElements();

}
