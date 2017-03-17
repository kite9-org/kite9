package org.kite9.diagram.position;

import java.util.List;

/**
 * This is used to hold the route of an edge, or container border.
 * 
 * @author robmoffat
 *
 */
public interface RouteRenderingInformation extends RenderingInformation {
	
	public static class Decoration {
		
		public Decoration() {
		}
		
		public Decoration(String name, Direction d, Dimension2D position) {
			super();
			this.name = name;
			this.d = d;
			this.position = position;
		}
		
		String name;
		Direction d;
		Dimension2D position;
		
	}

	public List<Dimension2D> getRoutePositions();
	
	public List<Boolean> getHops();

	public void reverse();
	
	public Decoration getFromDecoration();

	public void setFromDecoration(Decoration fromDecoration);

	public Decoration getToDecoration();

	public void setToDecoration(Decoration toDecoration);
	
	public Dimension2D getWaypoint(int pos);
	
	public void clear();
	
	public int size();
	
	public void add(Dimension2D d);
	
	public boolean isHop(int pos);
	
	public void setHop(int pos);

	public boolean isContradicting();

	public void setContradicting(boolean b);

}