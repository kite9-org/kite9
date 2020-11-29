package org.kite9.diagram.visualization.planarization.mgt.router;

import org.kite9.diagram.common.elements.RoutingInfo;

/**
 * Describes how we are allowed to route the edge with respect to the geographic
 * positions of the element's {@link RoutingInfo}.
 * 
 * STRICT means that the elements must be separated, and there must be space between them
 * in the expected direction for the edge to travel.
 * 
 * @author robmoffat
 *
 */
public enum GeographyType { STRICT, RELAXED }
