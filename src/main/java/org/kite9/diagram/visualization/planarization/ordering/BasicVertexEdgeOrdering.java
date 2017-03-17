package org.kite9.diagram.visualization.planarization.ordering;

import java.util.LinkedList;
import java.util.List;

import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Tools;

public class BasicVertexEdgeOrdering extends AbstractListBasedEdgeOrdering implements VertexEdgeOrdering {
	
	List<Edge> underlying;
	Vertex v;

	public BasicVertexEdgeOrdering(List<Edge> edges, Vertex v) {
		this.underlying = edges;
		this.v = v;
		initDirections();
	}

	private void initDirections() {
		directions = null;
		for (Edge edge : underlying) {
			addEdgeDirection(edge.getDrawDirectionFrom(v), Tools.isUnderlyingContradicting(edge));
		}
	}

	public BasicVertexEdgeOrdering() {
		underlying = new LinkedList<Edge>();
	}


	@Override
	public void remove(Edge toRemove) {
		underlying.remove(toRemove);
		initDirections();
	}

	@Override
	public void replace(Edge b, Edge a) {
		List<Edge> edgesAsList = getEdgesAsList();
		int bIndex = edgesAsList.indexOf(b);
		if (bIndex!=-1) {
			edgesAsList.set(bIndex, a);
		}
		changed();
	}

	@Override
	public List<Edge> getEdgesAsList() {
		return underlying;
	}

	@Override
	protected Direction getInterceptDirection(Edge e) {
		return e.getDrawDirectionFrom(v);
	}
	
}
