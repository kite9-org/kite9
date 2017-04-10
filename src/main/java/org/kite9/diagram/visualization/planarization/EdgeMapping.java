package org.kite9.diagram.visualization.planarization;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.logging.LogicException;

/**
 * Stores the details of how an edge maps back to a connection within
 * the planarization
 *
 */
public class EdgeMapping {
	
	public static final boolean TEST_AFTER_CHANGE = true;

	public LinkedList<Edge> getEdges() {
		return edges;
	}
	
	DiagramElement underlying;

	LinkedList<Edge> edges;

	public EdgeMapping(DiagramElement und, Edge e) {
		this.edges = new LinkedList<Edge>();
		this.edges.add(e);
		this.underlying=und;
		checkTest();
	}
	
	public EdgeMapping(DiagramElement und, LinkedList<Edge> e) {
		this.edges = e;
		this.underlying = und;
		checkTest();
	}
	
	public void replace(Edge replace, Edge a, Edge b) {
		checkTest();
		ListIterator<Edge> it = edges.listIterator();
		Edge before = null;
		Edge after = null;
		Edge current = null;
		while (it.hasNext()) {
			before = current;
			current = it.next();
			after = peek(it);
			if (current == replace) {
				if (nullOrMeets(a, before) && (nullOrMeets(b, after))) {
					it.previous();
					it.set(b);
					it.add(a);
					checkTest();
				} else if (nullOrMeets(b, before) && (nullOrMeets(a, after))) {
					it.previous();
					it.set(a);
					it.add(b);
					checkTest();
				} else {
					throw new LogicException("Can't figure out replacement order");
				}
			}
		}
	}

	private Edge peek(ListIterator<Edge> it) {
		if (it.hasNext()) {
			Edge out = it.next();
			it.previous();
			return out;
		} else {
			return null;
		}
	}

	private boolean nullOrMeets(Edge item, Edge meeting) {
		if (meeting == null) {
			return true;
		}
			
		return item.meets(meeting);
	}

	public void remove(Edge b) {
		edges.remove(b);
		checkTest();
	}
	
	public void remove(Collection<Edge> edges) {
		this.edges.removeAll(edges);
		checkTest();
	}

	public void add(Edge e2) {
		edges.add(e2);
		checkTest();
	}
	
	private void checkTest() {
		if (TEST_AFTER_CHANGE) {
			if (edges.size() == 0)
				return;
			
			Set<Vertex> met = new UnorderedSet<Vertex>();
			//System.out.println("CHECKTEST for: "+underlying+"\n"+this);
			
			Vertex f = getStartVertex();
			for (Edge e : edges) {
				if (e.meets(f)) {
					f = e.otherEnd(f);
					
					if (met.contains(f)) {
						throw new LogicException("Edge route already visits "+f);
					}
					
					met.add(f);
				} else {
					throw new LogicException("EdgeMapping is broken "+edges+" since "+e+" doesn't meet "+f);
				}
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[EdgeMapping: \n");
		for (Edge e : edges) {
			sb.append("  ");
			sb.append(e);
			sb.append("\t");
			sb.append(e.isReversed());
			sb.append("\t");
			sb.append(e.isReversed() ? e.getTo() : e.getFrom());
			sb.append("\t");
			sb.append(e.isReversed() ? e.getFrom() : e.getTo());
			sb.append("\n");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public Vertex getStartVertex() {
		Iterator<Edge> iterator = getEdges().iterator();
		return firstVertex(iterator);
	}
	
	public Vertex getEndVertex() {
		Iterator<Edge> iterator = getEdges().descendingIterator();
		return firstVertex(iterator);
	}

	private Vertex firstVertex(Iterator<Edge> iterator) {
		Edge first = iterator.next();
		if (getEdges().size()==1) {
			return first.getFrom();
		}
		
		Vertex start = null;
		Edge second = iterator.next();
		boolean commonFrom = second.meets(first.getFrom());
		boolean commonTo = second.meets(first.getTo());
		
		if (commonFrom && commonTo){
			// doesn't matter.
			return first.getFrom();
		} else if (commonFrom) {
			start = first.getTo();
		} else if (commonTo) {
			start = first.getFrom();
		} else {
			throw new LogicException("Can't determine start of edge map");
		}
		return start;
	}
}
