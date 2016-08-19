package org.kite9.diagram.visualization.orthogonalization.flow;

import org.kite9.diagram.common.elements.AbstractVertex;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.style.DiagramElement;

public class EdgeVertex {

	private static Vertex NULL_VERTEX =  new AbstractVertex("NULL") {
		
		@Override
		public DiagramElement getOriginalUnderlying() {
			return null;
		}
	};
	
	Edge edge;
	Vertex vertex = NULL_VERTEX;

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof EdgeVertex) {
			EdgeVertex ff = (EdgeVertex) arg0;
			return ff.edge.equals(edge) && ff.vertex.equals(vertex);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return edge.hashCode() + vertex.hashCode();
	}

	public String toString() {
		return edge.toString() + "-" + vertex.getID();
	}
}