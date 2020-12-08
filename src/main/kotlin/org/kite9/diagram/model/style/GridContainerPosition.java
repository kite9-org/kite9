package org.kite9.diagram.model.style;

import org.kite9.diagram.common.range.IntegerRange;

public class GridContainerPosition implements ContainerPosition {

	private final IntegerRange x;

	private final IntegerRange y;
	
	public GridContainerPosition(IntegerRange x, IntegerRange y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public IntegerRange getX() {
		return x;
	}

	public IntegerRange getY() {
		return y;
	}

	@Override
	public String toString() {
		return "["+x.getFrom()+","+x.getTo()+","+y.getFrom()+","+y.getTo()+"]";
	}
	
	

}
