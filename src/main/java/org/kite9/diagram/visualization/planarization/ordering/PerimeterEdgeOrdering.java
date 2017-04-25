package org.kite9.diagram.visualization.planarization.ordering;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.edge.SingleElementPlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
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

	public PlanarizationEdge getLeaverBeforeBorder(PlanarizationEdge e) {
		Vertex v = e.isReversed() ? e.getTo() : e.getFrom();
		
		do {
			VertexEdgeOrdering veo = (VertexEdgeOrdering) pln.getEdgeOrderings().get(v);
			Iterator<PlanarizationEdge> vit = veo.getIterator(false, e, e, false);
			vit.next();
			PlanarizationEdge currentLeaver = vit.next();
			if ((currentLeaver instanceof SingleElementPlanarizationEdge) && (((SingleElementPlanarizationEdge)currentLeaver).getOriginalUnderlying() != c)) {
				return currentLeaver;
			} else {
				e = currentLeaver;
				v = e.otherEnd(v);
			}
		} while (true);
	}


	@Override
	protected List<PlanarizationEdge> getEdgesAsListInner() {
		List<PlanarizationEdge> out = new LinkedList<PlanarizationEdge>(); 
		EdgeMapping em = pln.getEdgeMappings().get(c);
		
		Vertex start = em.getStartVertex();
		Vertex on = start;
		Iterator<PlanarizationEdge> it = em.getEdges().iterator();
		PlanarizationEdge current = em.getEdges().getLast();
		PlanarizationEdge next = it.next();
		
		while (next!=null) {
			// from on, look for leaving edges
			VertexEdgeOrdering veo = (VertexEdgeOrdering) pln.getEdgeOrderings().get(on);
			Iterator<PlanarizationEdge> vit = veo.getIterator(true, current, current, false);
			vit.next();
			PlanarizationEdge currentLeaver = vit.next();
			//System.out.println("Container "+c+" Edges Leaving "+on);
			while (!currentLeaver.isPartOf(c)) {
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
		
		if (from.isPartOf(c)) {
			return e.getDrawDirectionFrom(from);
		}
		
		Vertex to = e.getTo();
		if (to.isPartOf(c)) {
			return e.getDrawDirectionFrom(to);
		}
		
		throw new LogicException("Couldn't find intercept of "+e+" with container "+c);
	}



}
