/**
 * 
 */
package org.kite9.diagram.common.algorithms.so;

import java.util.ArrayList;
import java.util.List;

import org.kite9.framework.logging.LogicException;

public class Slideable {

	int canonicalOrder;
	int positionalOrder;
	private AlignStyle alignStyle;
	private Slideable alignTo;
	int minPosition = 0;
	Integer maxPosition = null;
	private Integer mdCacheMaxPosition = null;
	private Slideable mdCacheSlideable;
	private AbstractSlackOptimisation<?> so;

	// stores details of how this segment interacts with the others
	public List<Slideable> minRight = new ArrayList<Slideable>();

	public Slideable(AbstractSlackOptimisation<?> so, Object u, AlignStyle alignStyle) {
		this.underneath = u;
		this.alignStyle = alignStyle;
		this.so = so;
	}

	public List<Slideable> getMinRight() {
		return minRight;
	}

	public List<Slideable> getMinLeft() {
		return minLeft;
	}

	List<Slideable> minLeft = new ArrayList<Slideable>();
	List<Slideable> maxRight = new ArrayList<Slideable>();
	List<Slideable> maxLeft = new ArrayList<Slideable>();

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

	public void checkBounds(int min, Integer maxPosition2, Slideable cache) {
		if (maxPosition2 == null)
			return;

		if (min <= maxPosition2) {
			return;
		}

		throw new LogicException("Min " + min + " > Max " + maxPosition2 + ": " + this);
	}

	/**
	 * Called when something to the right increases minimum position
	 */
	private void _increasedMinimumOfRight(Slideable x) {
		Integer maxDist = so.getMaximumDistance(this, x);

		int newMin = x.minPosition - maxDist;

		increaseMinimum(newMin);

		so.pushCount++;
	}

	/**
	 * Called when something to the left increases minimum position
	 */
	private void _increasedMinimumOfLeft(Slideable x) {
		Integer minDist = so.getMinimumDistance(x, this);
		int newMin = x.minPosition + minDist;

		increaseMinimum(newMin);

		so.pushCount++;
	}

	public void increaseMinimum(int newMin) {
		if (newMin > minPosition) {

			so.log.send(so.log.go() ? null : "Increased to " + newMin + ": " + this);
			checkBounds(newMin, this.maxPosition, null);
			this.minPosition = newMin;

			// push others
			for (int i = 0; i < minRight.size(); i++) {
				minRight.get(i)._increasedMinimumOfLeft(this);
			}

			for (int i = 0; i < maxLeft.size(); i++) {
				maxLeft.get(i)._increasedMinimumOfRight(this);
			}
		}
	}

	/**
	 * Called when something to the left decreases maximum position
	 */
	private void _decreasedMaximumOfLeft(Slideable x, Slideable cache) {
		Integer maxDist = so.getMaximumDistance(x, this);
		int newMax = x.getMax(cache) + maxDist;
		decreaseMaximum(newMax, cache);

		so.pushCount++;
	}
	
	public void decreaseMaximum(int newMax) {
		decreaseMaximum(newMax, null);
	}

	void decreaseMaximum(int newMax, Slideable cache) {
		Integer max = getMax(cache);  
		if ((max == null) || (newMax < max)) {

			so.log.send(so.log.go() ? null : "Decreased to " + newMax + ": " + this);
			checkBounds(this.minPosition, newMax, cache);
			
			if (cache==null) {
				this.maxPosition = newMax;
			} else {
				this.mdCacheMaxPosition = newMax;
				this.mdCacheSlideable = cache;
			}

			// push others
			for (int i = 0; i < maxRight.size(); i++) {
				maxRight.get(i)._decreasedMaximumOfLeft(this, cache);
			}

			for (int i = 0; i < minLeft.size(); i++) {
				minLeft.get(i)._decreasedMaximumOfRight(this, cache);
			}
		}
	}

	private Integer getMax(Slideable cache) {
		return cache == null ? maxPosition : (cache==mdCacheSlideable ? mdCacheMaxPosition : maxPosition);
	}

	/**
	 * Called when something to the right decreases maximum position
	 */
	private void _decreasedMaximumOfRight(Slideable x, Slideable cache) {
		Integer minDist = so.getMinimumDistance(this, x);
		int newMax = x.getMax(cache) - minDist;

		decreaseMaximum(newMax,  cache);

		so.pushCount++;
	}

	/**
	 * Works out how much closer the current slideable can get to s
	 */
	public int minimumDistanceTo(Slideable s) {
		s.decreaseMaximum(s.minPosition, this);
		
		if (this.mdCacheMaxPosition == null) {
			if (this.maxPosition != null) {
				return s.minPosition - this.maxPosition;
				
			} else {
				return 0;
			}
		}
		
		return s.mdCacheMaxPosition - this.mdCacheMaxPosition;
	}
	

	@Override
	public String toString() {
		return "<" + so.getIdentifier(underneath) + " " + minPosition + "," + maxPosition + ">";
	}
	


	public Object getUnderlying() {
		return underneath;
	}

	public void setAlignTo(Slideable rs) {
		this.alignTo = rs;
	}

	public int getMinimumPosition() {
		return minPosition;
	}

	public Integer getMaximumPosition() {
		return maxPosition;
	}

	public AbstractSlackOptimisation<?> getSlackOptimisation() {
		return so.getSelf();
	}

}