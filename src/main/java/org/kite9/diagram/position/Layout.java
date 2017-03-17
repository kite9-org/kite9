package org.kite9.diagram.position;

/**
 * Extra layout options for Containers
 * 
 * @author robmoffat
 *
 */
public enum Layout {
	
	HORIZONTAL, VERTICAL, LEFT, RIGHT, UP, DOWN, GRID;

	public static Layout reverse(Layout d) {
		if (d==null)
			return null;
		switch (d) {
		case HORIZONTAL:
			return HORIZONTAL;
		case VERTICAL:
			return VERTICAL;
		case LEFT: 
			return RIGHT;
		case RIGHT:
			return LEFT;
		case UP:
			return DOWN;
		default:
			return UP;
		}
	}
	
	public static Layout rotateClockwise(Layout d) {
		if (d==null)
			return null;
		switch (d) {
		case HORIZONTAL:
			return VERTICAL;
		case VERTICAL:
			return HORIZONTAL;
		case LEFT: 
			return UP;
		case RIGHT:
			return DOWN;
		case UP:
			return RIGHT;
		default:
			return LEFT;
		}
	}
	
	public static Layout rotateAntiClockwise(Layout d) {
		if (d==null)
			return null;
		switch (d) {
		case HORIZONTAL:
			return VERTICAL;
		case VERTICAL:
			return HORIZONTAL;
		case LEFT: 
			return DOWN;
		case RIGHT:
			return UP;
		case UP:
			return LEFT;
		default:
			return RIGHT;
		}
	}
}

