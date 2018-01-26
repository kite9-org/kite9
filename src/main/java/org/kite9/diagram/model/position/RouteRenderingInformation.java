package org.kite9.diagram.model.position;

import java.util.List;

/**
 * This is used to hold the route of an edge, or container border.
 * 
 * @author robmoffat
 *
 */
public interface RouteRenderingInformation extends RenderingInformation {

	public List<Dimension2D> getRoutePositions();
	
	public List<Boolean> getHops();
	
	public Dimension2D getWaypoint(int pos);
	
	public void clear();
	
	public int size();
	
	public void add(Dimension2D d);
	
	public boolean isHop(int pos);
	
	public void setHop(int pos);

	public boolean isContradicting();

	public void setContradicting(boolean b);

}