package org.kite9.diagram.common.algorithms.so;

import java.util.HashMap;
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

	Map<SingleDirection, Integer> forward = new HashMap<>();
	Map<SingleDirection, Integer> backward = new HashMap<>();
	
	private Integer cachePosition;
	private Object cacheItem;
	
	private PositionChangeNotifiable owner;
	private boolean increasing;
	
	public SingleDirection(PositionChangeNotifiable owner, boolean increasing) {
		this.owner = owner;
		this.increasing = increasing;
	}
	
	private void update(int newPos, Object ci, boolean changedConstraints) {
		if (this.cacheItem != ci) {
			this.cacheItem = ci;
			this.cachePosition = position;
		}
		
		boolean moved = (cachePosition == null) || (increasing ? cachePosition < newPos : cachePosition > newPos);
		
		if ((moved) || (changedConstraints)) {
			cachePosition = newPos;
			
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
			
		} 
		
		if (ci == null) {
			position = cachePosition;
			owner.changedPosition(position);
		}
	}
	
	public Integer getPosition() {
		return position;
	}
	
	public void increasePosition(int pos) {
		update(pos, null, false);
	}
	
	/**
	 * Slack is how far you can push *this* without moving ci.
	 * 
	 * To do this, we push the position of this up to ci, and work out how far ci moves.
	 * If there is slack, ci will move less than *this*, and that's the slack.
	 */
	public int minimumDistanceTo(SingleDirection ci) {
		int startPosition = ci.position == null ? 1000 : ci.position;
		this.update(startPosition, ci, false);		
		return Math.abs(ci.cachePosition - startPosition);
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