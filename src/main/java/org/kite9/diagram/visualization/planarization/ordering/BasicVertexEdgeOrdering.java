package org.kite9.diagram.visualization.planarization.ordering;

import java.util.LinkedList;
import java.util.List;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Tools;

public class BasicVertexEdgeOrdering extends AbstractListBasedEdgeOrdering implements VertexEdgeOrdering {
	
	List<PlanarizationEdge> underlying;
	Vertex v;

	public BasicVertexEdgeOrdering(List<PlanarizationEdge> edges, Vertex v) {
		this.underlying = edges;
		this.v = v;
		initDirections();
	}

	private void initDirections() {
		directions = null;
		for (PlanarizationEdge edge : underlying) {
			addEdgeDirection(edge.getDrawDirectionFrom(v), Tools.isUnderlyingContradicting(edge));
		}
	}

	public BasicVertexEdgeOrdering() {
		underlying = new LinkedList<PlanarizationEdge>();
	}


	@Override
	public void remove(PlanarizationEdge toRemove) {
		underlying.remove(toRemove);
		initDirections();
	}

	@Override
	public void replace(PlanarizationEdge b, PlanarizationEdge a) {
		List<PlanarizationEdge> edgesAsList = getEdgesAsList();
		int bIndex = edgesAsList.indexOf(b);
		if (bIndex!=-1) {
			edgesAsList.set(bIndex, a);
		}
		changed();
	}

	@Override
	public List<PlanarizationEdge> getEdgesAsList() {
		return underlying;
	}

	@Override
	protected Direction getInterceptDirection(Edge e) {
		return e.getDrawDirectionFrom(v);
	}
	
}
