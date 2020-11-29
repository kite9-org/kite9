package org.kite9.diagram.visualization.planarization.mgt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.ordering.AbstractCachingEdgeOrdering;
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering;

public class MGTVertexEdgeOrdering extends AbstractCachingEdgeOrdering implements VertexEdgeOrdering {
	
	MGTPlanarization pl;

	Vertex v;
	
	List<PlanarizationEdge> af;
	List<PlanarizationEdge> ab;
	List<PlanarizationEdge> bf;
	List<PlanarizationEdge> bb;
	
	public MGTVertexEdgeOrdering(MGTPlanarization pl, Vertex v) {
		this.pl = pl;
		this.v = v;
		this.af = pl.getAboveForwardLinks(v);
		this.bf = pl.getBelowForwardLinks(v);
		this.bb = pl.getBelowBackwardLinks(v);
		this.ab = pl.getAboveBackwardLinks(v);
	}

	
	@Override
	public void remove(PlanarizationEdge toRemove) {
		safeRemove(toRemove, af);
		safeRemove(toRemove, bf);
		safeRemove(toRemove, bb);
		safeRemove(toRemove, ab);
		changed();
	}
	
	private void safeRemove(PlanarizationEdge toRemove, List<PlanarizationEdge> l) {
		if (l!=null) {
			l.remove(toRemove);
		}
	}

	
	@Override
	public int size() {
		return af.size()+bf.size()+bb.size()+ab.size();
	}
	
	@Override
	protected List<PlanarizationEdge> getEdgesAsListInner() {
		List<PlanarizationEdge> out = new ArrayList<PlanarizationEdge>(size());
		addAllBackwards(out, af);
		out.addAll(bf);
		addAllBackwards(out, bb);
		out.addAll(ab);
		
		directions = null;
		for (Edge edge : out) {
			addEdgeDirection(edge.getDrawDirectionFrom(v), Tools.isUnderlyingContradicting(edge));
		}
		
		return Collections.unmodifiableList(out);
	}

	private void addAllBackwards(List<PlanarizationEdge> out, List<PlanarizationEdge> af2) {
		for (int i = af2.size()-1; i >=0; i--) {
			out.add(af2.get(i));
		}
	}

	@Override
	public String toString() {
		return "[VEO:"+(getEdgeDirections()==MUTLIPLE_DIRECTIONS ? "MULTI" : getEdgeDirections())+":"+getEdgesAsList()+"]";
	}


	@Override
	public void replace(PlanarizationEdge b, PlanarizationEdge a) {
		replace(af, b, a);
		replace(bf, b, a);
		replace(bb, b, a);
		replace(ab, b, a);
		changed();
	}
	
	private boolean replace(List<PlanarizationEdge> in, PlanarizationEdge b, PlanarizationEdge a) {
		int bIndex = in.indexOf(b);
		if (bIndex!=-1) {
			in.set(bIndex, a);
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected Direction getInterceptDirection(Edge e) {
		return e.getDrawDirectionFrom(v);
	}

	
}
