package org.kite9.diagram.common.algorithms.so;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles the constraints for a {@link Slideable} in a single direction (e.g. 
 * increasing or decreasing).
 * 
 * @author robmoffat
 *
 */
class SingleDirection {
	
	private Integer position = null;

	Map<SingleDirection, Integer> forward = new LinkedHashMap<>();
	Map<SingleDirection, Integer> backward = new LinkedHashMap<>();
	
	private Integer cachePosition;
	private Object cacheItem;
	
	private PositionChangeNotifiable owner;
	private boolean increasing;
	
	public SingleDirection(PositionChangeNotifiable owner, boolean increasing) {
		this.owner = owner;
		this.increasing = increasing;
	}
	
	private void update(int newPos, Object ci, boolean changedConstraints) {
		if ((this.cacheItem != ci) || (ci == null)) {
			this.cacheItem = ci;
			this.cachePosition = position;
		}
		
		boolean moved = (cachePosition == null) || (increasing ? cachePosition < newPos : cachePosition > newPos);
		
		if ((moved) || (changedConstraints)) {
			cachePosition = newPos;
//			System.out.println("moving: "+this+" to "+newPos);
			
			for (SingleDirection fwd : forward.keySet()) {
				int dist = forward.get(fwd);
				int newPositionFwd = increasing ? cachePosition + dist : cachePosition - dist;
				fwd.update(newPositionFwd, ci, false);
			}
			
			for (SingleDirection bck : backward.keySet()) {
				Integer dist = backward.get(bck);
				int newPositionBck = increasing ? cachePosition - dist : cachePosition + dist;
				bck.update(newPositionBck, ci, false);
			}
			
			if (ci == null) {
				position = cachePosition;
				owner.changedPosition(position);
			}
		} 
	}
	
	public Integer getPosition() {
		return position;
	}
	
	public void increasePosition(int pos) {
		update(pos, null, false);
	}
	
	/**
	 * Works out minimum distance to ci, given that our item is in a certain start position.
	 */
	public Integer minimumDistanceTo(SingleDirection ci, int startPosition) {
		Object cacheMarker = new Object();
		this.update(startPosition, cacheMarker, false);
		if (ci.cacheItem != cacheMarker) {
			if (ci.position == null) {
				return null;
			} else {
				return increasing ? ci.position - this.position : this.position - ci.position;
			}
		} else {
			return Math.abs(ci.cachePosition - startPosition);
		}
	}

	
	void addForwardConstraint(SingleDirection to, int distance) {
		Integer existing = forward.get(to);
		if ((existing == null) || (existing < distance)) {
			forward.put(to, distance);
			
			if (position != null) {
				update(position, null, true);
			}
		}
	}
	
	void addBackwardConstraint(SingleDirection to, int distance) {
		Integer existing = backward.get(to);
		if ((existing == null) || (existing > distance)) {
			backward.put(to, distance);
			
			if (position != null) {
				update(position, null, true);
			}
		}
	}
	
	public PositionChangeNotifiable getOwner() {
		return owner;
	}

	@Override
	public String toString() {
		return owner.toString();
	}


	
}