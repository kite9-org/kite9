package org.kite9.diagram.visualization.orthogonalization.flow;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.visualization.planarization.Face;

public class FaceVertex {

	Face face;
	Vertex vertex;
	Edge prior;
	Edge after;

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof FaceVertex) {
			FaceVertex ff = (FaceVertex) arg0;
			return (ff.face == face) && (ff.vertex == vertex) && (ff.prior == prior) && (ff.after == after);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return face.hashCode() + vertex.hashCode() + prior.hashCode() + after.hashCode();
	}

	public String toString() {
		return face.getID() + "-" + vertex.getID() + "-" + prior + getUnderlying(prior) + "-" + after + getUnderlying(after);
	}

	private String getUnderlying(Edge e) {
		return ((PlanarizationEdge)e).getDiagramElements().keySet().toString();
	}
}