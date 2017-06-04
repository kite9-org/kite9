package org.kite9.diagram.visualization.orthogonalization.edge;

import java.util.Set;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.vertex.AbstractVertex;
import org.kite9.diagram.common.elements.vertex.NoElementVertex;
import org.kite9.diagram.model.DiagramElement;

/**
 * Special marker vertex that allows us to represent fan turns.
 * 
 * @author robmoffat
 *
 */
public class FanVertex extends AbstractVertex implements NoElementVertex {
	
	public FanVertex(String id) {
		super(id);
	}

	@Override
	public Set<DiagramElement> getDiagramElements() {
		return null;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return false;
	}
	
}