package org.kite9.diagram.common.algorithms.so;

import java.util.LinkedHashMap;
import java.util.Map;

import org.kite9.framework.logging.LogicException;

/**
 * Handles the constraints for a {@link Slideable} in a single direction (e.g. 
 * increasing or decreasing).
 * 
 * This is a DAG, and we should be able to prove it.
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
		try {
			if (this.cacheItem != ci) {
				this.cacheItem = ci;
				this.cachePosition = position;
			}
			
			boolean moved = (cachePosition == null) || (increasing ? cachePosition < newPos : cachePosition > newPos);
			
			if ((moved) || (changedConstraints)) {
//				System.out.println("moving: "+this+" to "+newPos);
				cachePosition = newPos;
//				System.out.println("(fwd)");
				for (SingleDirection fwd : forward.keySet()) {
					int dist = forward.get(fwd);
					int newPositionFwd = increasing ? cachePosition + dist : cachePosition - dist;
					fwd.update(newPositionFwd, ci, false);
				}

//				System.out.println("(bck)");
				for (SingleDirection bck : backward.keySet()) {
					Integer dist = backward.get(bck);
					int newPositionBck = increasing ? cachePosition - dist : cachePosition + dist;
					bck.update(newPositionBck, ci, false);
				}
//				System.out.println("(done)");
				
				if (ci == null) {
					position = cachePosition;
					owner.changedPosition(position);
				}
			}
		} catch (StackOverflowError e) {
			throw new LogicException("Couldn't adjust (SO): "+this+" pos: "+position+" cachePos: "+cachePosition);
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

	public int getMaxDepth() {
		int depth = 0;
		for (SingleDirection fwd : forward.keySet()) {
			depth = Math.max(depth, fwd.getMaxDepth()+1);
		}
		
		for (SingleDirection back : backward.keySet()) {
			depth = Math.max(depth, back.getMaxDepth()+1);
		}
		
		return depth;
	}
	

	/**
	 * For multiple {@link SingleDirection} elements at the same position, work out if there is an ordering.
	 * We only consider stuff at the same position because if it's not at the same position, just compare
	 * the positions directly.
	 */
	public boolean hasTransitiveForwardConstraintTo(SingleDirection d, Integer position) {
		if (this.position != position) {
			return false;
		}
		
		for (SingleDirection to : forward.keySet()) {
			if (to == d) {
				return true;
			} else if (to.hasTransitiveForwardConstraintTo(d, position)) {
				return true;
			}
		}
		
		return false;
	}
	
}