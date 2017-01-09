package org.kite9.diagram.common.algorithms.so;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.kite9.diagram.position.Direction;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * Holds a bunch of {@link Slideable}s and can return them in position order, which is not exact, because
 * sometimes they can overlap one another.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractSlackOptimisation<X> implements Logable {

	protected Kite9Log log = new Kite9Log(this);

	public int pushCount = 0;
	public int maxCount = 0;
	protected Collection<Slideable> allSlideables = new LinkedHashSet<>();

	public void updatePositionalOrdering() {
		positionalOrder = new ArrayList<Slideable>(allSlideables);
		Collections.sort(positionalOrder, new Comparator<Slideable>() {
	
			public int compare(Slideable o1, Slideable o2) {
				Integer p1 = o1.getMinimumPosition();
				Integer p2 = o2.getMinimumPosition();
				if (p1 == null) {
					return -1;
				} else if (p2 == null) {
					return 1;
				} else {
					int out = ((Integer) p1).compareTo(p2);
					if (out == 0) {
						// in the event of a tie, work it out from dependency
						if (o1.hasTransitiveForwardConstraintTo(o2)) {
							return -1;
						} else if (o1.hasTransitiveForwardConstraintTo(o1)){
							return 1;
						} else {
							return 0;
						}
					}
					
		
					return out;
				}
			}
		});
	
		for (int i = 0; i < positionalOrder.size(); i++) {
			Slideable slideable = positionalOrder.get(i);
			slideable.positionalOrder = i;
		}
	}

	public abstract String getIdentifier(Object underneath);

	private List<Slideable> positionalOrder;

	public Collection<Slideable> getAllSlideables() {
		return allSlideables;
	}

	protected Direction d;

	public List<Slideable> getPositionalOrder() {
		return positionalOrder;
	}

	public AbstractSlackOptimisation() {
		super();
	}

	public Direction getDirection() {
		return d;
	}

	public void ensureMinimumDistance(Slideable left, Slideable right, int minLength) {
		log.send(log.go() ? null : "Updating min distance to " + minLength + " for " + left + " to " + right);
		left.addMinimumForwardConstraint(right, minLength);
		right.addMaximumForwardConstraint(left, minLength);
	}

	public void ensureMaximumDistance(Slideable left, Slideable right, int maxLength) {
		if (left.positionalOrder > right.positionalOrder) {
			throw new LogicException("Left and Right wrong way round? " + left + " " + right);
		}
		
		log.send(log.go() ? null : "Updating max distance to " + maxLength + " for " + left + " to " + right);
		right.addMinimumBackwardConstraint(left, maxLength);
		left.addMaximumBackwardConstraint(right, maxLength);
	}

	public void addSlideables(Collection<Slideable> s) {
		for (Slideable slideable : s) {
			allSlideables.add(slideable);
		}
	
		for (Slideable slideable : s) {
			addedSlideable(slideable);
		}
	}
	
	public void addSlideables(Slideable... s) {
		for (Slideable slideable : s) {
			allSlideables.add(slideable);
		}
	
		for (Slideable slideable : s) {
			addedSlideable(slideable);
		}
	}
	
	
	protected abstract void addedSlideable(Slideable s);

	
	public int getPushCount() {
		return pushCount;
	}

	public String getPrefix() {
		return "SLOP";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

	protected AbstractSlackOptimisation<X> getSelf() {
		return this;
	}

	public String toString() {
		return "SlackOptimisation:" + getDirection();
	}

	public abstract void initialiseSlackOptimisation();


}