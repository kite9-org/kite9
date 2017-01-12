/**
 * 
 */
package org.kite9.diagram.common.algorithms.so;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

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
	 * 
	 * We can't move the element any further than the max position without breaking other 
	 * constraints.
	 */
	public int minimumDistanceTo(Slideable s) {
		Integer maxSet = this.getMaximumPosition();
		maxSet = maxSet == null ? 1000 : maxSet;		// 
		Integer slack1 = minimum.minimumDistanceTo(s.minimum, maxSet);
		so.log.send("Calculating minimum distance from "+this+" to "+s+" "+slack1);
		Integer slack2 = s.maximum.minimumDistanceTo(maximum, s.getMinimumPosition());
		so.log.send("Calculating minimum distance from "+s+" to "+this+" "+slack2);
		if (slack2 == null) {
			return slack1;
		}
		
//		if (slack1.intValue() != slack2.intValue()) {
//			throw new LogicException("Something went wrong");
//		}
		return Math.max(slack1, slack2);
		
		//return slack1;
	}
	
	public boolean hasTransitiveForwardConstraintTo(Slideable s2) {
		return minimum.hasTransitiveForwardConstraintTo(s2.minimum, s2.getMinimumPosition());
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
	
	public void withMinimumForwardConstraints(Consumer<Slideable> action) {
		for (SingleDirection sd : minimum.forward.keySet()) {
			action.accept((Slideable) sd.getOwner());
		}
	}
	
	public void withMaximumForwardConstraints(Consumer<Slideable> action) {
		for (SingleDirection sd : maximum.forward.keySet()) {
			action.accept((Slideable) sd.getOwner());
		}
	}

	void addMinimumForwardConstraint(Slideable to, int dist) {
		minimum.addForwardConstraint(to.minimum, dist);
	}
	
	void addMinimumBackwardConstraint(Slideable to, int dist) {
		minimum.addBackwardConstraint(to.minimum, dist);
	}
	
	void addMaximumForwardConstraint(Slideable to, int dist) {
		maximum.addForwardConstraint(to.maximum, dist);
	}
	
	void addMaximumBackwardConstraint(Slideable to, int dist) {
		maximum.addBackwardConstraint(to.maximum, dist);
	}

	public void setMinimumPosition(int i) {
		minimum.increasePosition(i);
	}
	
	public void setMaximumPosition(int i) {
		maximum.increasePosition(i);
	}

	public Set<Slideable> getForwardSlideables(boolean increasing) {
		Set<Slideable> out = new HashSet<>();
		if (increasing) {
			for (SingleDirection sd : minimum.forward.keySet()) {
				out.add((Slideable) sd.getOwner());
			}
		} else {
			for (SingleDirection sd : maximum.forward.keySet()) {
				out.add((Slideable) sd.getOwner());
			}
		}
		
		return out;
	}

	

}