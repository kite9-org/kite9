package org.kite9.diagram.common.elements.vertex;

import org.kite9.diagram.model.DiagramElement;

/**
 * Vertex which is part of a representing a single diagram element 
 * (e.g. part of a connection)
 * @author robmoffat
 *
 */
public interface SingleElementVertex extends Vertex {

	public DiagramElement getOriginalUnderlying();
	
}
