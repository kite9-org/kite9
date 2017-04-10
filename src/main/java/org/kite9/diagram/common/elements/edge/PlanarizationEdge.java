package org.kite9.diagram.common.elements.edge;

import org.kite9.diagram.common.elements.vertex.Vertex;

/**
 * Edge interface implemented by all of the Edges created by the planarization process.
 * @author robmoffat
 *
 */
public interface PlanarizationEdge extends Edge {

	/**
	 * Unlinks the edge from the planarization.  
	 */
	public void remove();

	enum RemovalType { YES, NO, TRY }
	
	/**
	 * Indicates that the edge is just a temporary edge used only during the planarization process
	 */
	public RemovalType removeBeforeOrthogonalization();

	/**
	 * Indicates the cost of introducing an edge crossing this one, used in {@link CrossingEdgeInserterTransform}.
	 * @return
	 */
	public int getCrossCost();
	
	/**
	 * Indicates the cost of introducing a bend on this edge, relative to other edges.
	 * Note that directed edges generally wont accept bends at all.
	 */
	public int getBendCost();
	
	/** 
	 * Weighting of the cost of adding length to this edge.
	 */
	public int getLengthCost();
	
	/**
	 * Returns true if this edge heads through the planarization in a straight line
	 */
	public boolean isStraightInPlanarization();
	
	/**
	 * Returns true if the edge is enforcing a particular layout in the orthogonalization
	 * step.  Indicates that no other edges should ideally leave or enter the same side.
	 */
	public boolean isLayoutEnforcing();
	
	public void setLayoutEnforcing(boolean le);
	
	/**
	 * Performs a split on the current edge.  Returns 2 edge attr.  The original edge should then be discarded.
	 */
	public PlanarizationEdge[] split(Vertex toIntroduce);
	
}