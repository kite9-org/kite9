package org.kite9.diagram.visualization.planarization.ordering;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.kite9.diagram.common.elements.Edge;

/**
 * Used where the underlying order is not based on a list, but one would nevertheless come in handy.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractCachingEdgeOrdering extends AbstractEdgeOrdering {

	private List<Edge> cache;
		
	@Override
	public List<Edge> getEdgesAsList() {
		if (cache != null) {
			return cache;
		}
		
		this.cache = Collections.unmodifiableList(getEdgesAsListInner());
		return cache;
	}

	protected abstract List<Edge> getEdgesAsListInner();

	@Override
	public Object getEdgeDirections() {
		// prepare the cache first
		getEdgesAsList();
		return super.getEdgeDirections();
	}

	@Override
	public void changed() {
		this.cache = null;
		super.changed();
	}

	@Override
	public int size() {
		return getEdgesAsList().size();
	}

	@Override
	public Iterator<Edge> getIterator(final boolean clockwise, final Edge startingAt, Edge finish, boolean directedOnly) {
		
		final List<Edge> underlying = getEdgesAsList();

		return new AbstractEdgeIterator(clockwise, startingAt, finish, directedOnly) {
		
			int i = underlying.indexOf(startingAt);

			@Override
			public Edge getNext() {
				i = (i + (clockwise ? 1 : -1) + underlying.size()) % underlying.size();
				return underlying.get(i);
			}
		};
	}
}
