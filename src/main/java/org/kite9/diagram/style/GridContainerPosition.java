package org.kite9.diagram.style;

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

}
