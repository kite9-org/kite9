package org.kite9.diagram.visualization.orthogonalization.vertex;

import java.util.Set;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.AbstractVertex;
import org.kite9.diagram.common.elements.vertex.NoElementVertex;
import org.kite9.diagram.model.DiagramElement;

/**
 * Special marker vertex that allows us to represent the join points for
 * darts/vertices constructed by the VertexArranger.
 * 
 * This keeps track of the underlying PLanarizationEdge that needs to meet from
 * the OrthBuilder process.
 * 
 * @author robmoffat
 *
 */
public class ExternalVertex extends AbstractVertex implements NoElementVertex {

	private PlanarizationEdge joins;
	
	public ExternalVertex(String id, PlanarizationEdge joins) {
		super(id);
		this.joins = joins;
	}

	@Override
	public Set<DiagramElement> getDiagramElements() {
		return null;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return false;
	}
	
	protected boolean joins(PlanarizationEdge e) {
		return e==joins;
	}
	
}