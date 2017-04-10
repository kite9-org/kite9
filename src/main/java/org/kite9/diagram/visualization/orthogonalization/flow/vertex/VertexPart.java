package org.kite9.diagram.visualization.orthogonalization.flow.vertex;

import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.visualization.orthogonalization.flow.vertex.ConstrainedVertexFlowOrthogonalizer.VertexDivision;

/**
 * Memento for part of a vertex in the flow graph.
 * 
 * @author robmoffat
 *
 */
public class VertexPart {

	Vertex v;
	VertexDivision e;
	
	public VertexPart(Vertex v, VertexDivision e) {
		super();
		this.v = v;
		this.e = e;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((e == null) ? 0 : e.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VertexPart other = (VertexPart) obj;
		if (e == null) {
			if (other.e != null)
				return false;
		} else if (!e.equals(other.e))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}
	
	
	
}