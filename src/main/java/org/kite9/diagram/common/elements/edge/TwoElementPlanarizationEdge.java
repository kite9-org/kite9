package org.kite9.diagram.common.elements.edge;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;

/**
 * Used at the interface between two diagram elements (typically, in a grid).
 * @author robmoffat
 *
 */
public interface TwoElementPlanarizationEdge extends PlanarizationEdge {

	public DiagramElement getOtherSide(DiagramElement from);
	
	public DiagramElement getElementForSide(Direction d);
}
