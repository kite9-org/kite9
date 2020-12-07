package org.kite9.diagram.visualization.planarization.ordering;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.logging.Kite9Log;

/**
 * Tracks directed and undirected edges arriving at a vertex or container, and the directions they arrive at.
 * @author robmoffat
 *
 */
public interface EdgeOrdering {

	public static final Object MUTLIPLE_DIRECTIONS = new Object();

	/**
	 * Returns either a {@link Direction}, or the object MULTIPLE_DIRECTIONS
	 */
	public abstract Object getEdgeDirections();

	public abstract int size();

	public abstract Iterator<PlanarizationEdge> getIterator(boolean clockwise, PlanarizationEdge startingAt, PlanarizationEdge finish, boolean directedOnly);

	/**
	 * Tells the ordering that things have changed.
	 */
	public abstract void changed();

	/**
	 * Allows you to figure out if a given directed edge can be inserted into the ordering
	 * @param after Another DIRECTED edge in the ordering
	 * @param d the direction you want to insert
	 * @param clockwise whether you are inserting clockwise or anti-clockwise relative to after
	 */
	public boolean canInsert(PlanarizationEdge after, Direction d, boolean clockwise, Kite9Log log);

	/**
	 * Returns an unmodifiable list of leaving edges in clockwise order.
	 */
	public List<PlanarizationEdge> getEdgesAsList();
	
	/**
	 * Returns set of underlying diagram element leavers
	 */
	public Set<DiagramElement> getUnderlyingLeavers();

}