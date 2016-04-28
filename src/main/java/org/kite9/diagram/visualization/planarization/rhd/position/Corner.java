/**
 * 
 */
package org.kite9.diagram.visualization.planarization.rhd.position;

import org.kite9.diagram.common.objects.BasicBounds;
import org.kite9.diagram.common.objects.Bounds;

public enum Corner {
	
	TOP_RIGHT, BOTTOM_LEFT, FINISH;
	
	public BoundsBasedPositionRoutingInfo operate(BoundsBasedPositionRoutingInfo from, BoundsBasedPositionRoutingInfo ob) {
		switch (this) {
		case TOP_RIGHT:
			return new BoundsBasedPositionRoutingInfo(max(from.x, ob.x), 
					min(from.y, ob.y));
		case BOTTOM_LEFT:
			return new BoundsBasedPositionRoutingInfo(min(from.x, ob.x), 
					max(from.y, ob.y));
		case FINISH:
			return new BoundsBasedPositionRoutingInfo(
					narrow(from.x, ob.x), 
					narrow(from.y, ob.y));
		default:
			return from;
		}
	}

	private Bounds max(Bounds a, Bounds b) {
		return new BasicBounds(Math.max(a.getDistanceMin(), b.getDistanceMax()) , Math.max(a.getDistanceMax(), b.getDistanceMax()));		
	}
	
	private Bounds min(Bounds a, Bounds b) {
		return new BasicBounds(Math.min(a.getDistanceMin(), b.getDistanceMin()) , Math.min(a.getDistanceMax(), b.getDistanceMin()));		
	}	
	private Bounds narrow(Bounds a, Bounds b) {
		if (a.getDistanceMax() < b.getDistanceMin()) {
			return new BasicBounds(b.getDistanceMin(), b.getDistanceMin());
		} else if (a.getDistanceMin() > b.getDistanceMax()) {
			return new BasicBounds(b.getDistanceMax(), b.getDistanceMax());
		} else {
			return new BasicBounds(Math.max(a.getDistanceMin(), b.getDistanceMin()), Math.min(a.getDistanceMax(), b.getDistanceMax()));
		}
	}
	
}