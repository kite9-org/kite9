package org.kite9.diagram.visualization.orthogonalization.flow;

import java.util.Set;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.AbstractVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.DiagramElement;

public class EdgeVertex {

	private static Vertex NULL_VERTEX =  new AbstractVertex("NULL") {

		@Override
		public Set<DiagramElement> getDiagramElements() {
			return null;
		}

		@Override
		public boolean isPartOf(DiagramElement de) {
			return false;
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