package org.kite9.diagram.visualization.display;

import org.kite9.diagram.model.Connection;
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
	 * Returns the necessary distance between two diagram attr, possibly connected by an optional /along/.
	 * If concave is set to false, then actually, we are not looking at a and b facing each other, and the distance should be entirely due to 
	 * the along part.
	 */
	public double getMinimumDistanceBetween(DiagramElement a, Direction aSide, DiagramElement b, Direction bSide, Direction direction, DiagramElement along, boolean concave);

	/**
	 * Determines whether you should draw a hop at the point connection a meets connection b
	 */
	public boolean addHop(Connection a, Connection b);
}
