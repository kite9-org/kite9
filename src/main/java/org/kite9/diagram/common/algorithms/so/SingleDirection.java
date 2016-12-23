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

	Map<SingleDirection, Integer> minForward = new HashMap<>();
	Map<SingleDirection, Integer> maxBackward = new HashMap<>();
	
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
			
			for (SingleDirection fwd : minForward.keySet()) {
				int dist = minForward.get(fwd);
				int newPositionFwd = increasing ? cachePosition + dist : cachePosition - dist;
				fwd.update(newPositionFwd, ci, false);
			}
			
			for (SingleDirection bck : maxBackward.keySet()) {
				Integer dist = maxBackward.get(bck);
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
	public Integer slackTo(SingleDirection ci) {
		if ((this.position == null) || (ci.position == null)) {
			return null;
		}

		int currentDistance = increasing ? 
				ci.position - this.position : this.position - ci.position;
		
		this.update(ci.position, ci, false);
		int newDistance = increasing ? ci.cachePosition - this.cachePosition :
			this.cachePosition - ci.cachePosition;
		
		return currentDistance - newDistance;
	}

	
	public void addForwardConstraint(SingleDirection to, int distance) {
		Integer existing = minForward.get(to);
		if ((existing == null) || (existing < distance)) {
			minForward.put(to, distance);
			
			if (position != null) {
				update(position, null, true);
			}
		}
	}
	
	public void addBackwardConstraint(SingleDirection to, int distance) {
		Integer existing = maxBackward.get(to);
		if ((existing == null) || (existing > distance)) {
			maxBackward.put(to, distance);
			
			if (position != null) {
				update(position, null, true);
			}
		}
	}
	
	public PositionChangeNotifiable getOwner() {
		return owner;
	}


	
}