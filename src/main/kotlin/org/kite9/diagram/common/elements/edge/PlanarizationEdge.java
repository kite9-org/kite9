package org.kite9.diagram.common.elements.edge;

import java.util.Map;

import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;

/**
 * Edge interface implemented by all of the Edges created by the planarization process.
 * 
 * @author robmoffat
 *
 */
public interface PlanarizationEdge extends Edge {

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
	
	/**
	 * Gives you information about the elements surrounding this one, and (potentially) which side
	 * they are on.
	 */
	public Map<DiagramElement, Direction> getDiagramElements();
	

	public void setFrom(Vertex v);

	public void setTo(Vertex v);
		
	/**
	 * Unlinks the edge from the from, to vertices it is connected to.
	 */
	public void remove();
	
	/**
	 * If drawDirection is set, this will be the reverse of draw direction.
	 */
	public Direction getFromArrivalSide();
	
	/**
	 * If drawDirection is set, this will be the same as draw direction.
	 */
	public Direction getToArrivalSide();


}