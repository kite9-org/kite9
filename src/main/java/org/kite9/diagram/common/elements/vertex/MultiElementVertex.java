package org.kite9.diagram.common.elements.vertex;

import org.kite9.diagram.model.DiagramElement;

/**
 * Vertex which is involved in multiple diagram elements. e.g. grid
 * 
 * @author robmoffat
 *
 */
public interface MultiElementVertex extends Vertex {

	public boolean isPartOf(DiagramElement c);
}
