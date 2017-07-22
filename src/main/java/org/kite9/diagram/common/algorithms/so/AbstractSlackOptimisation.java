package org.kite9.diagram.common.algorithms.so;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.kite9.framework.common.Kite9ProcessingException;
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
	protected Collection<Slideable<X>> allSlideables = new LinkedHashSet<>();


	public abstract String getIdentifier(Object underneath);

	public Collection<Slideable<X>> getAllSlideables() {
		return allSlideables;
	}

	public AbstractSlackOptimisation() {
		super();
	}

	public void ensureMinimumDistance(Slideable<X> left, Slideable<X> right, int minLength) {
		if (left.getSlackOptimisation() != right.getSlackOptimisation()) {
			throw new Kite9ProcessingException("Mixing dimensions");
		}

		try {
			left.addMinimumForwardConstraint(right, minLength);
			right.addMaximumForwardConstraint(left, minLength);
			log.send(log.go() ? null : "Updated min distance to " + minLength + " for " + left + " to " + right);
		} catch (LogicException e) {
			debugOutput(true);
		}
	}

	private void debugOutput(boolean minimums) {
		Set<Slideable<X>> alreadyDone = new HashSet<>();
		for (Slideable<X> slideable : allSlideables) {
			if (!alreadyDone.contains(slideable)) {
				debugOutputSlideable(minimums, slideable, alreadyDone, 0);
			}
		}
	}

	private void debugOutputSlideable(boolean minimums, Slideable<X> slideable, Set<Slideable<X>> alreadyDone, int indent) {
		log.send(indent, slideable.toString());
		if (!alreadyDone.contains(slideable)) {
			alreadyDone.add(slideable);
			for (Slideable<X> s2 : slideable.getForwardSlideables(minimums)) {
				debugOutputSlideable(minimums, s2, alreadyDone, indent+2);
			}
		}
	}

	public void ensureMaximumDistance(Slideable<X> left, Slideable<X> right, int maxLength) {
		if (left.getSlackOptimisation() != right.getSlackOptimisation()) {
			throw new Kite9ProcessingException("Mixing dimensions");
		}
		
		try {
			log.send(log.go() ? null : "Updating max distance to " + maxLength + " for " + left + " to " + right);
			right.addMinimumBackwardConstraint(left, maxLength);
			left.addMaximumBackwardConstraint(right, maxLength);
		} catch (LogicException e) {
			debugOutput(false);
		}
	}

	public void addSlideables(Collection<Slideable<X>> s) {
		for (Slideable<X> slideable : s) {
			allSlideables.add(slideable);
		}
	
		for (Slideable<X> slideable : s) {
			addedSlideable(slideable);
		}
	}
	
	public void addSlideables(Slideable<X>... s) {
		for (Slideable<X> slideable : s) {
			allSlideables.add(slideable);
		}
	
		for (Slideable<X> slideable : s) {
			addedSlideable(slideable);
		}
	}
	
	
	protected abstract void addedSlideable(Slideable<X> s);

	
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

	public abstract void initialiseSlackOptimisation();


}