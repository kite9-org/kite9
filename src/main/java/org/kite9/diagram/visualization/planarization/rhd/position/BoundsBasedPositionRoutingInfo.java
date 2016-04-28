package org.kite9.diagram.visualization.planarization.rhd.position;

import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader.Routing;

public class BoundsBasedPositionRoutingInfo extends PositionRoutingInfo {
	
	Bounds x, y;
	Rectangle2D rect;
	
	public BoundsBasedPositionRoutingInfo(Bounds x, Bounds y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public double centerX() {
		return x.getDistanceCenter();
	}

	@Override
	public double centerY() {
		return y.getDistanceCenter();
	}

	@Override
	public double getMinX() {
		return x.getDistanceMin();
	}

	@Override
	public double getMaxX() {
		return x.getDistanceMax();
	}

	@Override
	public double getMinY() {
		return y.getDistanceMin();
	}

	@Override
	public double getMaxY() {
		return y.getDistanceMax();
	}

	@Override
	public double getWidth() {
		return getMaxX() - getMinX();
	}

	@Override
	public double getHeight() {
		return getMaxY() - getMinY();
	}

	@Override
	public int compareTo(RoutingInfo arg0) {
		BoundsBasedPositionRoutingInfo bbri = (BoundsBasedPositionRoutingInfo) arg0;
		int yc = y.compareTo(bbri.y);
		if (yc != 0) {
			return yc;
		} else {
			int xc = x.compareTo(bbri.x);
			return xc;
		}
	}
	
	private boolean breaksOrder;

	@Override
	public boolean isBreakingOrder() {
		return breaksOrder;
	}

	@Override
	public int compareX(RoutingInfo with) {
		return x.compareTo(((BoundsBasedPositionRoutingInfo)with).x);
	}

	@Override
	public int compareY(RoutingInfo with) {
		return y.compareTo(((BoundsBasedPositionRoutingInfo)with).y);
	}
	
	public Map<Routing, Corner> getAvoidanceCorners() {
		return ac;
	}
	
	private Map<Routing, Corner> ac;
	
	public void setAvoidanceCorners(Map<Routing, Corner> ac) {
		this.ac = ac;
	}
}