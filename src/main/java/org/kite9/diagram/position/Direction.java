package org.kite9.diagram.position;

public enum Direction {
	UP, LEFT, DOWN, RIGHT;

	public Turn getDirectionChange(Direction to) {
		int change = (to.ordinal() - this.ordinal() + 4) % 4;
		return Turn.values()[change];	
	}
	
	public static Direction rotateAntiClockwise(Direction d) {
		int ord = d.ordinal();
		ord = ord + 1;
		ord = (ord) % 4;
		return Direction.values()[ord];
	}
	
	public static Direction rotateClockwise(Direction d) {
		int ord = d.ordinal();
		ord = ord + 3;
		ord = (ord) % 4;
		return Direction.values()[ord];
	}
	
	public static Direction reverse(Direction d) {
		if (d==null)
			return null;
		int ord = d.ordinal();
		ord = ord + 2;
		ord = (ord) % 4;
		return Direction.values()[ord];
	}
	
	public static Direction getDirection(String s) {
		if ((s==null) || (s.trim().length() == 0)) {
			return null;
		} else {
			return Direction.valueOf(s);
		}
	}
	
	public static boolean isHorizontal(Direction d) {
		return d==LEFT || d== Direction.RIGHT;
	}
	
	public static boolean isVertical(Direction d) {
		return d==UP || d== Direction.DOWN;
	}

}
