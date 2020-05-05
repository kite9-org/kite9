package org.kite9.diagram.model.style;

import org.kite9.diagram.model.position.Direction;

public enum LabelPlacement {

	TOP, LEFT, BOTTOM, RIGHT, TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, BOTTOM_RIGHT;

	public boolean matches(Direction d) {
		switch (this) {
		case TOP:
			return d == Direction.UP;
		case LEFT:
			return d == Direction.LEFT;
		case RIGHT:
			return d == Direction.RIGHT;
		case TOP_LEFT:
		case TOP_RIGHT:
		case BOTTOM_LEFT:
		case BOTTOM_RIGHT:
			return false;
		case BOTTOM:
		default:
			return d == Direction.DOWN;
		}
	}
	
}
