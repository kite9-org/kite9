/**
 * 
 */
package org.kite9.diagram.position;

public enum HPos {
	LEFT, RIGHT;

	public Direction getDirection() {
		if (this.ordinal() == 0) {
			return Direction.LEFT;
		} else {
			return Direction.RIGHT;
		}
	}
	
	public static HPos getFromDirection(Direction d) {
		switch (d) {
		case LEFT:
			return LEFT;
		case RIGHT:
			return RIGHT;
		default:
			return null;
		}
	}
}