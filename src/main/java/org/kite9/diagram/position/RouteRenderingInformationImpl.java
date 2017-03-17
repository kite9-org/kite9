package org.kite9.diagram.position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteRenderingInformationImpl extends AbstractRenderingInformationImpl implements RouteRenderingInformation {

	private List<Dimension2D> routePositions = new ArrayList<Dimension2D>();
	private List<Boolean> hops = new ArrayList<>();
	private boolean contradicting;
	
	public List<Dimension2D> getRoutePositions() {
		return routePositions;
	}

	public List<Boolean> getHops() {
		return hops;
	}
	

	public boolean isHop(int pos) {
		return pos >= hops.size() ? false : hops.get(pos);
	}

	public void setHop(int pos) {
		while (hops.size() <= pos) {
			hops.add(false);
		}
		hops.set(pos, true);
	}

	public void reverse() {
		Collections.reverse(hops);
		Collections.reverse(routePositions);
	}

	public Decoration getFromDecoration() {
		return null;
	}

	public void setFromDecoration(Decoration fromDecoration) {
	}

	public Decoration getToDecoration() {
		return null;
	}

	public void setToDecoration(Decoration toDecoration) {
	}

	public Dimension2D getWaypoint(int pos) {
		return getRoutePositions().get(pos);
	}

	public void clear() {
		getRoutePositions().clear();
		getHops().clear();
	}

	public int size() {
		return getRoutePositions().size();
	}

	public void add(Dimension2D d) {
		getRoutePositions().add(d);
	}

	public boolean isContradicting() {
		return contradicting;
	}

	public void setContradicting(boolean b) {
		this.contradicting = b;
	}
	
	private void ensureSizeAndPosition() {
		double minx = Double.MAX_VALUE, maxx= 0 , miny = Double.MAX_VALUE, maxy =0;;
		for (Dimension2D d : routePositions) {
			minx = Math.min(d.x(), minx);
			miny = Math.min(d.y(), miny);
			maxx = Math.max(d.x(), maxx);
			maxy = Math.max(d.y(), maxy);
		}
		
		this.setPosition(new Dimension2D(minx, miny));
		this.setSize(new Dimension2D(maxx - minx, maxy -miny));
	}

	@Override
	public Dimension2D getPosition() {
		ensureSizeAndPosition();
		return super.getPosition();
	}

	@Override
	public Dimension2D getSize() {
		ensureSizeAndPosition();
		return super.getSize();
	}
	
	

}
