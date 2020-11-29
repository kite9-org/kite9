package org.kite9.diagram.common.algorithms.so;

import java.util.LinkedHashMap;
import java.util.Map;

import org.kite9.diagram.logging.LogicException;

/**
 * Handles the constraints for a {@link Slideable} in a single direction (e.g. 
 * increasing or decreasing).
 * 
 * This is a DAG, and we should be able to prove it.
 * 
 * @author robmoffat
 *
 */
public class SingleDirection {
	
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
		if (increasing) {
			position = 0;
		}
	}
	
	static class QuitOnChange {
		SingleDirection on;
	}
	
	private boolean update(int newPos, Object ci, boolean changedConstraints) {
		try {
			if ((this.cacheItem == ci) && (ci instanceof QuitOnChange) && (((QuitOnChange)ci).on == this)) {
				// we've visited here before - return false if we move
				return  (increasing ? cachePosition >= newPos : cachePosition <= newPos);
			}
			
			if (this.cacheItem != ci) {
				this.cacheItem = ci;
				this.cachePosition = position;
			}
						
			boolean moved = (cachePosition == null) || (increasing ? cachePosition < newPos : cachePosition > newPos);
			
			boolean ok = true;
			if ((moved) || (changedConstraints)) {
//				System.out.println("moving: "+this+" to "+newPos);
				cachePosition = newPos;
//				System.out.println("(fwd)");
				for (SingleDirection fwd : forward.keySet()) {
					int dist = forward.get(fwd);
					int newPositionFwd = increasing ? cachePosition + dist : cachePosition - dist;
					ok = ok && fwd.update(newPositionFwd, ci, false);
				}

//				System.out.println("(bck)");
				for (SingleDirection bck : backward.keySet()) {
					Integer dist = backward.get(bck);
					int newPositionBck = increasing ? cachePosition - dist : cachePosition + dist;
					ok = ok && bck.update(newPositionBck, ci, false);
				}
//				System.out.println("(done)");
				
				if (ci == null) {
					position = cachePosition;
					owner.changedPosition(position);
				}
			}
			
			return ok;
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
	 * Returns null if the elements aren't connected.
	 */
	public Integer minimumDistanceTo(SingleDirection ci, int startPosition) {
		Object cacheMarker = new Object();
		this.update(startPosition, cacheMarker, false);
		if (ci.cacheItem != cacheMarker) {
			// the two elements are independent, one doesn't push the other.
			return null;
		} 
		
		return Math.abs(ci.cachePosition - startPosition);
	}
	
	public boolean canAddForwardConstraint(SingleDirection to, int distance) {
		Integer existing = forward.get(to);
		QuitOnChange qoc = new QuitOnChange();
		qoc.on = this;
		if ((existing == null) || (existing < distance)) {
			int curPos;
			if (this.getPosition() == null) {
				this.update(0, qoc, false);
				curPos = 0;
			} else {
				curPos = this.getPosition();
				this.update(curPos, qoc, false);
			}
			int newPos = increasing ? curPos + distance : curPos - distance;
			return to.update(newPos, qoc, true);
		}
		
		return true;
	}
		
	public void addForwardConstraint(SingleDirection to, int distance) {
		Integer existing = forward.get(to);
		if ((existing == null) || (existing < distance)) {
			forward.put(to, distance);
			
			if (position != null) {
				update(position, null, true);
			}
		}
	}
	
	public void addBackwardConstraint(SingleDirection to, int distance) {
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
	
	public boolean hasForwardConstraints() {
		return forward.size() > 0;
	}
	
	public boolean hasBackwardConstraints() {
		return backward.size() > 0;
	}
	
}