package org.kite9.diagram.common.elements.edge;

import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.DiagramElement;

public interface BiDirectionalPlanarizationEdge extends PlanarizationEdge {

	public DiagramElement getOriginalUnderlying();
	
	public Connected getFromConnected();
	
	public Connected getToConnected();
}
