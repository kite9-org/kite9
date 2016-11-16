package org.kite9.diagram.visualization.planarization.grid;

import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertices;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

/**
 * Handles positioning of elements for {@link Layout}.GRID.  
 * 
 * 
 * @author robmoffat
 *
 */
public interface GridPositioner {

	/**
	 * Works out the position of the elements within the grid for a given container.
	 * 
	 * Should cache too.
	 * 
	 * @param allowSpanning Set to true if we should consider the full span of the "occupies-x" and "occupies-y" directive, or just the lower bound.
	 */
	public DiagramElement[][] placeOnGrid(Container ord, boolean allowSpanning);
	
	/**
	 * Returns the XPosition as a pair of fractions, showing where a given element has been 
	 * placed within the grid.
	 */
	public OPair<BigFraction> getGridXPosition(DiagramElement elem);
	
	/**
	 * Returns the XPosition as a pair of fractions, showing where a given element has been 
	 * placed within the grid.
	 */
	public OPair<BigFraction> getGridYPosition(DiagramElement elem);

	/**
	 * Given a laid-out container (i.e. post phase 2 of RHD) this works out, for each fraction used on Grid X/Y positions, 
	 * where within the bounds of the container's PositionInfo the fractions should be placed.
	 * @param containerVertices 
	 * @param bounds 
	 */
	public OPair<Map<BigFraction, Double>> getFracMapForGrid(Container c, RoutableHandler2D rh, ContainerVertices containerVertices, RoutingInfo bounds);
}