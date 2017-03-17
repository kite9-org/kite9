package org.kite9.framework.serialization;

import org.apache.batik.css.engine.value.AbstractValue;
import org.kite9.diagram.style.IntegerRange;

/**
 * An integer range, from and to.  
 * Used for grid positioning in each axis.
 * 
 * @author robmoffat
 *
 */
class IntegerRangeValue extends AbstractValue implements IntegerRange {

	private int from, to;
	
	@Override
	public int getFrom() {
		return from;
	}

	@Override
	public int getTo() {
		return to;
	}

	public IntegerRangeValue(int from, int to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public String getCssText() {
		return null;
	}

	public static boolean notSet(IntegerRangeValue in) {
		return (in == null) || (in.from == -1);
	}
}
