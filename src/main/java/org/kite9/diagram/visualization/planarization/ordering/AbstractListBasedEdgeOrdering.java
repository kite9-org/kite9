package org.kite9.diagram.visualization.planarization.ordering;

import java.util.Iterator;
import java.util.List;

import org.kite9.diagram.common.elements.edge.Edge;

/**
 * Implements a good deal of the functionality of the EdgeOrdering using the underlying list.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractListBasedEdgeOrdering extends AbstractEdgeOrdering {

	public AbstractListBasedEdgeOrdering() {
		super();
	}

	@Override
	public int size() {
		return getEdgesAsList().size();
	}

	@Override
	public Iterator<Edge> getIterator(final boolean clockwise, final Edge startingAt, Edge finish, boolean directedOnly) {

		return new AbstractEdgeIterator(clockwise, startingAt, finish, directedOnly) {
			List<Edge> underlying = getEdgesAsList();
			
			int i = underlying.indexOf(startingAt);

			@Override
			public Edge getNext() {
				i = (i + (clockwise ? 1 : -1) + underlying.size()) % underlying.size();
				return underlying.get(i);
			}
		};
	}

	@Override
	public String toString() {
		return "[VEO:"+(getEdgeDirections()==MUTLIPLE_DIRECTIONS ? "MULTI" : getEdgeDirections())+":"+getEdgesAsList()+"]";
	}

}