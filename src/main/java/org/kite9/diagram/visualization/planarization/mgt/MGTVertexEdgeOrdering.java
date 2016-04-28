package org.kite9.diagram.visualization.planarization.mgt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.ordering.AbstractCachingEdgeOrdering;
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering;

public class MGTVertexEdgeOrdering extends AbstractCachingEdgeOrdering implements VertexEdgeOrdering {
	
	MGTPlanarization pl;

	Vertex v;
	
	List<Edge> af;
	List<Edge> ab;
	List<Edge> bf;
	List<Edge> bb;
	
	public MGTVertexEdgeOrdering(MGTPlanarization pl, Vertex v) {
		this.pl = pl;
		this.v = v;
		this.af = pl.getAboveForwardLinks(v);
		this.bf = pl.getBelowForwardLinks(v);
		this.bb = pl.getBelowBackwardLinks(v);
		this.ab = pl.getAboveBackwardLinks(v);
	}

	
	@Override
	public void remove(Edge toRemove) {
		safeRemove(toRemove, af);
		safeRemove(toRemove, bf);
		safeRemove(toRemove, bb);
		safeRemove(toRemove, ab);
		changed();
	}
	
	private void safeRemove(Edge toRemove, List<Edge> l) {
		if (l!=null) {
			l.remove(toRemove);
		}
	}

	
	@Override
	public int size() {
		return af.size()+bf.size()+bb.size()+ab.size();
	}
	
	@Override
	protected List<Edge> getEdgesAsListInner() {
		List<Edge> out = new ArrayList<Edge>(size());
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

	private void addAllBackwards(List<Edge> out, List<Edge> af2) {
		for (int i = af2.size()-1; i >=0; i--) {
			out.add(af2.get(i));
		}
	}

	@Override
	public String toString() {
		return "[VEO:"+(getEdgeDirections()==MUTLIPLE_DIRECTIONS ? "MULTI" : getEdgeDirections())+":"+getEdgesAsList()+"]";
	}


	@Override
	public void replace(Edge b, Edge a) {
		replace(af, b, a);
		replace(bf, b, a);
		replace(bb, b, a);
		replace(ab, b, a);
		changed();
	}
	
	private boolean replace(List<Edge> in, Edge b, Edge a) {
		int bIndex = in.indexOf(b);
		if (bIndex!=-1) {
			in.set(bIndex, a);
			return true;
		} else {
			return false;
		}
	}


	@Override
	public void replaceAll(List<Edge> allNewEdges) {
		af.clear();
		bf.clear();
		bb.clear();
		ab.clear();
		bf.addAll(allNewEdges);
		changed();
	}


	@Override
	protected Direction getInterceptDirection(Edge e) {
		return e.getDrawDirectionFrom(v);
	}

	
}
