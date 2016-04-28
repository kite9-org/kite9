package org.kite9.diagram.common.algorithms.so;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.kite9.diagram.position.Direction;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * This class implements the basic algorithm for SlackOptimisation, where a single dimension is optimised.
 * Each "slidable" element has maximum and minimum bounds, computed by the constraints that apply within it,
 * Constraints are either minimum or maximum distances to other slideables.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractSlackOptimisation<X> implements Logable {

	protected Kite9Log log = new Kite9Log(this);

	public int pushCount = 0;
	public int maxCount = 0;
	protected List<Slideable> canonicalOrder;

	public void updatePositionalOrdering() {
		positionalOrder = new ArrayList<Slideable>(canonicalOrder);
		Collections.sort(positionalOrder, new Comparator<Slideable>() {
	
			public int compare(Slideable o1, Slideable o2) {
				Integer p1 = o1.getMinimumPosition();
				Integer p2 = o2.getMinimumPosition();
				int out = ((Integer) p1).compareTo(p2);
				if (out == 0) {
					// in the event of a tie, use the canonical ordering
					out = ((Integer) o1.canonicalOrder).compareTo(o2.canonicalOrder);
				}
	
				return out;
			}
		});
	
		for (int i = 0; i < positionalOrder.size(); i++) {
			Slideable slideable = positionalOrder.get(i);
			slideable.positionalOrder = i;
			// slideable.mdCacheSlideable = null;
			//System.out.println("PO: " + slideable);
		}
	}

	public abstract String getIdentifier(Object underneath);

	private List<Slideable> positionalOrder;

	public List<Slideable> getCanonicalOrder() {
		return canonicalOrder;
	}

	protected Direction d;

	public List<Slideable> getPositionalOrder() {
		return positionalOrder;
	}

	private Map<Slideable, Map<Slideable, Integer>> maximumDistancesLR = new HashMap<Slideable, Map<Slideable, Integer>>();
	private Map<Slideable, Map<Slideable, Integer>> minimumDistancesLR = new HashMap<Slideable, Map<Slideable, Integer>>();

	public AbstractSlackOptimisation() {
		super();
	}

	public Direction getDirection() {
		return d;
	}

	public void ensureMinimumDistance(Slideable left, Slideable right, int minLength, boolean push) {
		// this piece of code ensures that the minRight array is ordered largest
		// slideable first, which makes getMinimumDistance faster
		left.minRight.remove(right);
		ListIterator<Slideable> li = left.minRight.listIterator();
		while (li.hasNext()) {
			Slideable item = li.next();
			int dist = getMinimumDistance(left, item);
			if (dist < minLength) {
				// need to add before this element
				li.previous();
				break;
			}
		}
		li.add(right);
		if (!right.minLeft.contains(left)) {
			right.minLeft.add(left);
		}
	
		Map<Slideable, Integer> forSeg = minimumDistancesLR.get(left);
		if (forSeg == null) {
			forSeg = new HashMap<Slideable, Integer>();
			minimumDistancesLR.put(left, forSeg);
		}
	
		Integer distance = forSeg.get(right);
		if ((distance == null) || (minLength > distance)) {
			forSeg.put(right, minLength);
			if (push) {
				right.increaseMinimum(left.minPosition + minLength);
			}
			log.send(log.go() ? null : "Updating min distance to " + minLength + " for " + left + " to " + right);
		}
	}

	public Integer getMinimumDistance(Slideable left, Slideable right) {
		Map<Slideable, Integer> forSeg = minimumDistancesLR.get(left);
		if (forSeg == null)
			return null;
	
		Integer integer = forSeg.get(right);
		//log.send(log.go() ? null : "Min between "+left+" and "+right+" is "+integer ); 
		return integer;
	}

	public void ensureMaximumDistance(Slideable left, Slideable right, int maxLength, boolean push) {
		if (left.positionalOrder > right.positionalOrder) {
			throw new LogicException("Left and Right wrong way round? " + left + " " + right);
		}
		if (!left.maxRight.contains(right)) {
			left.maxRight.add(right);
		}
		if (!right.maxLeft.contains(left)) {
			right.maxLeft.add(left);
		}
	
		Map<Slideable, Integer> forSeg = maximumDistancesLR.get(left);
		if (forSeg == null) {
			forSeg = new HashMap<Slideable, Integer>();
			maximumDistancesLR.put(left, forSeg);
		}
	
		Integer distance = forSeg.get(right);
	
		if ((distance == null) || (maxLength < distance)) {
			forSeg.put(right, maxLength);
			if (push) {
				if (left.maxPosition != null) {
					right.decreaseMaximum(left.maxPosition + maxLength, null);
				}
			}
			log.send(log.go() ? null : "Updating max distance to " + maxLength + " for " + left + " to " + right);
		}
	}

	public Integer getMaximumDistance(Slideable left, Slideable right) {
		Map<Slideable, Integer> forSeg = maximumDistancesLR.get(left);
		if (forSeg == null)
			return null;
	
		Integer integer = forSeg.get(right);
		//log.send(log.go() ? null : "Max between "+left+" and "+right+" is "+integer ); 
		return integer;
	}

	private Slideable findIndependent(List<Slideable> toOrder, boolean left) {
		for (Slideable possible : toOrder) {
			Map<Slideable, Integer> deps = minimumDistancesLR.get(possible);
			if (deps == null) {
				return possible;
			}
	
			boolean ok = true;
	
			for (Slideable d2 : deps.keySet()) {
				if (toOrder.contains(d2)) {
					ok = false;
				}
			}
			if (ok) {
				log.send(log.go() ? null : "Found independent: " + possible);
				return possible;
			}
		}
	
		throw new LogicException("One of them should be independent: " + toOrder);
	}

	public void addSlideables(Slideable... s) {
		for (Slideable slideable : s) {
			canonicalOrder.add(slideable);
		}
	
		for (Slideable slideable : s) {
			ensureMinimumDistances(slideable, true);
		}
	}
	
	
	protected abstract void ensureMinimumDistances(Slideable s, boolean push);

	/**
	 * Recalculates the canonical ordering, if anything has changed.
	 */
	public void updateCanonicalOrdering() {
		List<Slideable> copy = canonicalOrder;
		canonicalOrder = new ArrayList<Slideable>(copy.size());
		while (copy.size() > 0) {
			// find an element in toOrder that does not have dependent in
			// toOrder
			Slideable chosen = findIndependent(copy, true);
			canonicalOrder.add(chosen);
			chosen.canonicalOrder = copy.size() - 1;
			copy.remove(chosen);
		}
	
		Collections.reverse(canonicalOrder);
	}

	public int getPushCount() {
		return pushCount;
	}

	public String getPrefix() {
		return "SLOP";
	}

	public boolean isLoggingEnabled() {
		return false;
	}

	protected AbstractSlackOptimisation<X> getSelf() {
		return this;
	}

	public String toString() {
		return "SlackOptimisation:" + getDirection();
	}

	public void initialiseSlackOptimisation() {
		Slideable first = canonicalOrder.get(0);
		first.minPosition = -1;
		first.increaseMinimum(0);
		first.maxPosition = 0;
	}


}