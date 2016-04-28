package org.kite9.diagram.visualization.planarization.mgt.router;

import java.util.List;

import org.kite9.diagram.common.elements.Routable;
import org.kite9.diagram.common.elements.RoutingInfo;

/**
 * Handles {@link Routable} for each element in the group process, where routables are the basic unit of
 * currency for the {@link AbstractRouteFinder}.
 * 
 * @author robmoffat
 *
 */
public interface RoutableReader {

	public RoutingInfo getPlacedPosition(Object r);

	public enum Routing { OVER_FORWARDS, UNDER_FORWARDS, OVER_BACKWARDS, UNDER_BACKWARDS };
	
	/**
	 * Performs a move operation, where the move is either above or below 'past'.
	 * linePosition can be provided null, in which case we are setting up a new route starting at past.
	 * If r is null, we are arriving at a destination.
	 */
	public LineRoutingInfo move(LineRoutingInfo current, RoutingInfo past, Routing r);

	
	public boolean isWithin(RoutingInfo area, RoutingInfo pos);

	/**
	 * Checks that to and from occupy the same horiz/vert plane
	 */
	public boolean isInPlane(RoutingInfo to, RoutingInfo from, boolean horiz);

	/**
	 * Creates a space containing all the area from/to
	 */
	public RoutingInfo increaseBounds(RoutingInfo a, RoutingInfo b);

	/**
	 * Returns true if there is a common intersection
	 */
	public boolean overlaps(RoutingInfo a, RoutingInfo b);
	
	public void initRoutableOrdering(List<? extends Object> items);
}
