package org.kite9.diagram.visualization.planarization.ordering;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.EdgeMapping;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.framework.logging.LogicException;

/**
 * Provides an edge ordering for all the edges leaving a diagram element, which is
 * not represented by a single vertex.
 * 
 * @author robmoffat
 *
 */
public class PerimeterEdgeOrdering extends AbstractCachingEdgeOrdering {
	
	Planarization pln;
	DiagramElement c;

	public PerimeterEdgeOrdering(Planarization pln, DiagramElement c) {
		this.pln = pln;
		this.c = c;
	}

	public Edge getLeaverBeforeBorder(Edge e) {
		Vertex v = e.isReversed() ? e.getTo() : e.getFrom();
		
		do {
			VertexEdgeOrdering veo = (VertexEdgeOrdering) pln.getEdgeOrderings().get(v);
			Iterator<Edge> vit = veo.getIterator(false, e, e, false);
			vit.next();
			Edge currentLeaver = vit.next();
			if (currentLeaver.getOriginalUnderlying() != c) {
				return currentLeaver;
			} else {
				e = currentLeaver;
				v = e.otherEnd(v);
			}
		} while (true);
	}


	@Override
	protected List<Edge> getEdgesAsListInner() {
		List<Edge> out = new LinkedList<Edge>();
		EdgeMapping em = pln.getEdgeMappings().get(c);
		
		Vertex start = em.getStartVertex();
		Vertex on = start;
		Iterator<Edge> it = em.getEdges().iterator();
		Edge current = em.getEdges().getLast();
		Edge next = it.next();
		
		while (next!=null) {
			// from on, look for leaving edges
			VertexEdgeOrdering veo = (VertexEdgeOrdering) pln.getEdgeOrderings().get(on);
			Iterator<Edge> vit = veo.getIterator(true, current, current, false);
			vit.next();
			Edge currentLeaver = vit.next();
			//System.out.println("Container "+c+" Edges Leaving "+on);
			while (currentLeaver.getOriginalUnderlying() != c) {
				out.add(currentLeaver);
				//System.out.println("   adding "+currentLeaver);
				currentLeaver = vit.next();
			}
			
			on = next.otherEnd(on);
			current = next;
			next = it.hasNext() ? it.next() : null;
		}
		
		// fix the edge direction indicator
		directions = null;
		for (Edge edge : out) {
			//System.out.println("edge="+edge+" con="+Tools.isUnderlyingContradicting(edge)+" dir="+edge.getDrawDirection());
			Direction d = getInterceptDirection(edge);
			addEdgeDirection(d, Tools.isUnderlyingContradicting(edge));
		}
		
		//System.out.println("Edges Leaving Container "+c+" "+out+ " with directions "+directions);
		
		return out;
	}

	@Override
	protected Direction getInterceptDirection(Edge e) {
		Vertex from = e.getFrom();
		
		if (Tools.isUnderlyingContradicting(e)) {
			return null;
		}
		
		if (from.getOriginalUnderlying()==c) {
			return e.getDrawDirectionFrom(from);
		}
		
		Vertex to = e.getTo();
		if (to.getOriginalUnderlying()==c) {
			return e.getDrawDirectionFrom(to);
		}
		
		throw new LogicException("Couldn't find intercept of "+e+" with container "+c);
	}



}
