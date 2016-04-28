package org.kite9.diagram.visualization.planarization.rhd.position;

import java.awt.image.BufferedImage;
import java.util.Collection;

import org.kite9.diagram.common.elements.Routable;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.primitives.HintMap;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader;

/**
 * Manages the position of objects in 2D space by considering 2D position individually.
 * This extends {@link RoutableReader} by adding the ability to set positions in a given 
 * axis.
 * 
 * @author robmoffat
 *
 */
public interface RoutableHandler2D extends RoutableReader {
	
	public static enum DPos { BEFORE, OVERLAP, AFTER };
	
	public Bounds getPosition(Object r, boolean horiz);
	
	public Bounds getTempPosition(Object r, boolean horiz);
	
	public void setPlacedPosition(Object r, Bounds ri, boolean horiz);
	
	public void setTempPosition(Object r, Bounds ri, boolean horiz);
	
	public void clearTempPositions(boolean horiz);
	
	public Bounds getTopLevelBounds(boolean horiz);
		
	public Bounds narrow(Layout layout, Bounds in, boolean horiz, boolean applyGutters);
	
	public RoutingInfo createRouting(Bounds x, Bounds y);

	public void outputSettings();

	public Bounds getBoundsOf(RoutingInfo ri, boolean horiz);
	
	public DPos compare(Object a, Object b, boolean horiz);

	public DPos compareBounds(Bounds ab, Bounds bb);
	
	public void setPlacedPosition(Object a, RoutingInfo cri);
	
	public boolean overlaps(RoutingInfo a, RoutingInfo b);
	
	public boolean overlaps(Bounds a, Bounds b);
	
	public double distance(RoutingInfo from, RoutingInfo to, boolean horiz);
	
	/**
	 * Expands the area in bounding to cover ri
	 */
	public RoutingInfo increaseBounds(RoutingInfo bounding, RoutingInfo ri);
		 
	/**
	 * Returns a routing which represents an empty area.
	 */
	public RoutingInfo emptyBounds();
	
	/**
	 * Returns true if the routing info given is the empty bounds.
	 */
	public boolean isEmptyBounds(RoutingInfo bounds);
	
	/**
	 * Works out distance cost of from -> to.
	 */
	public double cost(RoutingInfo from, RoutingInfo to);

	
	/**
	 * Determines the order the two RI's must be in for the planarization line.  returns -1, 1 or 0 if same
	 */
	public int order(RoutingInfo a, RoutingInfo b);
	
	/**
	 * Useful debug method.
	 */
	public BufferedImage drawPositions(Collection<? extends Routable> out);

	/**
	 * Reports the bounds into the hintmap object for future renderings
	 */
	public void setHints(HintMap hm, RoutingInfo bounds);

	/**
	 * Trims an existing bounds by a given amount
	 */
	public RoutingInfo narrow(RoutingInfo bounds, double vertexTrim);
	

}
