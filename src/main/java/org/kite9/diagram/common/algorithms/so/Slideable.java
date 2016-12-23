/**
 * 
 */
package org.kite9.diagram.common.algorithms.so;

import java.util.function.Function;

import org.kite9.framework.logging.LogicException;

public class Slideable implements PositionChangeNotifiable {
	
	int canonicalOrder;
	int positionalOrder;
	private AlignStyle alignStyle;
	private Slideable alignTo;
	private AbstractSlackOptimisation<?> so;
	
	private SingleDirection minimum = new SingleDirection(this, true);
	private SingleDirection maximum = new SingleDirection(this, false); 

	public Slideable(AbstractSlackOptimisation<?> so, Object u, AlignStyle alignStyle) {
		this.underneath = u;
		this.alignStyle = alignStyle;
		this.so = so;
	}

	public int getPositionalOrder() {
		return positionalOrder;
	}

	public Slideable getAlignTo() {
		return alignTo;
	}

	public AlignStyle getAlignStyle() {
		return alignStyle;
	}

	public void setAlignStyle(AlignStyle alignStyle) {
		this.alignStyle = alignStyle;
	}

	private Object underneath;

	/**
	 * Works out how much closer the current slideable can get to s.
	 * This works by fixing the position for s (temporarily, using the cache)
	 * and then taking the max position for *this*.
	 */
	public int minimumDistanceTo(Slideable s) {
		Integer slack1 = minimum.slackTo(s.minimum);
		return s.getMinimumPosition() - this.getMinimumPosition() - slack1;
	}
	
	

	@Override
	public String toString() {
		return "<" + so.getIdentifier(underneath) + " " + minimum.getPosition() + "," + maximum.getPosition() + ">";
	}

	public Object getUnderlying() {
		return underneath;
	}

	public void setAlignTo(Slideable rs) {
		this.alignTo = rs;
	}

	public Integer getMinimumPosition() {
		return minimum.getPosition();
	}

	public Integer getMaximumPosition() {
		return maximum.getPosition();
	}

	public AbstractSlackOptimisation<?> getSlackOptimisation() {
		return so.getSelf();
	}

	public void changedPosition(int pos) {
		so.pushCount ++;
		Integer min = getMinimumPosition();
		Integer max = getMaximumPosition();
		if ((min == null) || (max == null)) {
			return;
		} else {
			if (min > max) {
				throw new LogicException("Min " + min + " > Max " + max + ": " + this);
			}
		}
	}
	
	public void withMinimumForwardConstraints(Function<Slideable, Object> action) {
		for (SingleDirection sd : minimum.minForward.keySet()) {
			action.apply((Slideable) sd.getOwner());
		}
	}
	
	public void withMaximumForwardConstraints(Function<Slideable, Object> action) {
		for (SingleDirection sd : maximum.minForward.keySet()) {
			action.apply((Slideable) sd.getOwner());
		}
	}

	public void addMinimumForwardConstraint(Slideable to, int dist) {
		minimum.addForwardConstraint(to.minimum, dist);
	}
	
	public void addMinimumBackwardConstraint(Slideable to, int dist) {
		minimum.addBackwardConstraint(to.minimum, dist);
	}
	
	public void addMaximumForwardConstraint(Slideable to, int dist) {
		maximum.addForwardConstraint(to.maximum, dist);
	}
	
	public void addMaximumBackwardConstraint(Slideable to, int dist) {
		maximum.addBackwardConstraint(to.maximum, dist);
	}

	public void setMinimumPosition(int i) {
		minimum.increasePosition(i);
	}
	
	public void setMaximumPosition(int i) {
		maximum.increasePosition(i);
	}
}