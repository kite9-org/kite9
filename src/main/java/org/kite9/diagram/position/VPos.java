/**
 * 
 */
package org.kite9.diagram.position;

public enum VPos {
	
	
	UP, DOWN;
	
	
	
	public Direction getDirection() {
		if (this.ordinal()==0) {
			return Direction.UP;
		} else {
			return Direction.DOWN;
		}
	}
	
	public static VPos getFromDirection(Direction d) {
		switch (d) {
		case UP:
			return UP;
		case DOWN:
			return DOWN;
		default:
			return null;
		}
	}
}