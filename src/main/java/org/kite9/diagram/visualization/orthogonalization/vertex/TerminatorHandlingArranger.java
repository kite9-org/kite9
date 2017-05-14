package org.kite9.diagram.visualization.orthogonalization.vertex;

import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Terminator;

public class TerminatorHandlingArranger {

	protected Terminator getTerminator(Edge underlyingEdge, Vertex v) {
		DiagramElement underlyingDestination = v.getOriginalUnderlying();
		DiagramElement underlyingLink = underlyingEdge.getOriginalUnderlying();
		if (underlyingLink instanceof Connection) {
			boolean fromEnd = ((Connection) underlyingLink).getFrom() == underlyingDestination;
			boolean toEnd = ((Connection) underlyingLink).getTo() == underlyingDestination;
			
			if (fromEnd) {
				return ((Connection) underlyingLink).getFromDecoration();
			}
			
			if (toEnd) {
				return ((Connection) underlyingLink).getToDecoration();
			}
		}
		
		return null;
	}
	

}
