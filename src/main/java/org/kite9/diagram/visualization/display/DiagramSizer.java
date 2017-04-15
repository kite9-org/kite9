package org.kite9.diagram.visualization.display;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;

/**
 * Handles spacing between diagram components
 * 
 * @author robmoffat
 *
 */
public interface DiagramSizer {

	/**
	 * Returns the necessary distance between two diagram attr.
	 */
	public double getMinimumDistanceBetween(DiagramElement a, Direction aSide, DiagramElement b, Direction bSide, Direction direction, DiagramElement along);

}
