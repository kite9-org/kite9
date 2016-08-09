package org.kite9.diagram.visualization.planarization.ordering;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.LogicException;

public abstract class AbstractEdgeOrdering implements EdgeOrdering {

	protected Object directions;

	@Override
	public Object getEdgeDirections() {
		return directions;
	}
	
	public void addEdgeDirection(Direction d, boolean contradicting) {
		if (d==null) {
			return;
		}
		if (contradicting) {
			return;
		}
		if (directions==null) {
			directions = d;
		} else if (directions!=d) {
			directions = MUTLIPLE_DIRECTIONS;
		}
	}
		
	protected abstract Direction getInterceptDirection(Edge e);
	
	
	@Override
	public boolean canInsert(Edge before, Direction d, boolean clockwise, Kite9Log log) {
		if ((before.getDrawDirection() == null) || (Tools.isUnderlyingContradicting(before))) {
			// need to find directed edge
			Iterator<Edge> it = getIterator(!clockwise, before, null, true);
			it.next();
			before = it.next();
		}
		
		Iterator<Edge> it = getIterator(clockwise, before, null, true);
		it.next();
		Edge after = it.next();

		int turns =  turnsBetween(before, after, d, clockwise);
		log.send("Routing between "+before+" "+after+" going "+(clockwise ? "clockwise" : "anticlockwise") + " ok="+(turns<4));
		
		return turns < 4;

		
	}

	/**
	 * Turns round the circle and sees if introducing the turn will create a contradiction
	 */
	private int turnsBetween(Edge first, Edge next, Direction fromD, boolean clockwise) {
		Direction aboveDD = getInterceptDirection(first);
		Direction dd = fromD;
		int turns = 0;
		turns += turnTo(aboveDD, dd, clockwise);
		Direction belowDD = getInterceptDirection(next);
		turns += turnTo(dd, belowDD, clockwise);
		return turns;
	}
	
	private int turnTo(Direction start, Direction finish, boolean clockwise) {
		int turns = 0;
		if (start == finish) {
			return 0;
		}
		do {
			start = clockwise ? Direction.rotateClockwise(start) : Direction.rotateAntiClockwise(start);
			turns ++;
			if (start==finish) {
				return turns;
			}
		} while ((start != finish) || (turns > 4));
		
		throw new LogicException("can't turn to: "+finish);
	}

	
	protected abstract class AbstractEdgeIterator implements Iterator<Edge> {
		
		private Edge finish;
		private Edge next = null;
		private boolean directed;
		
		public AbstractEdgeIterator(boolean clockwise, Edge startingAt, Edge finish, boolean directedOnly) {
			this.next = startingAt;
			checkEdge(true);
			this.directed = directedOnly;
			this.finish = finish;
		}
		
		private void checkEdge(boolean init) {
			boolean ok;
			do {
				if (!init && (next == finish)) {
					next = null;
					return;
				}

				ok = true;
				if ((directed) && edgeIsNotDirected()) {
					ok = false;
				}
				
				if (!ok) {
					next = getNext();
				}
			} while (!ok);
		}

		private boolean edgeIsNotDirected() {
			return (next.getDrawDirection()==null) || (Tools.isUnderlyingContradicting(next));
		}

		public abstract Edge getNext(); 
		

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Edge next() {
			Edge out = next;
			next = getNext();
			checkEdge(true);
			return out;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}	
	
	
	
	@Override
	public void changed() {
		underlyingCache = null;
	}

	private Set<DiagramElement> underlyingCache;
	
	@Override
	public Set<DiagramElement> getUnderlyingLeavers() {
		if (underlyingCache==null) {
			List<Edge> edges = getEdgesAsList();
			underlyingCache = new UnorderedSet<DiagramElement>(edges.size() * 2);
			for (Edge e : edges) {
				DiagramElement und = e.getOriginalUnderlying();
				if (und != null) {
					underlyingCache.add(und);
				}
			}
		}
		return underlyingCache;
	}
}
