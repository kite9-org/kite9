package org.kite9.diagram.position;

public enum Turn {
	STRAIGHT(0), LEFT(-1), BACK(0), RIGHT(1);
	
	public int getTurnValue() {
		return turnValue;
	}
	
	private int turnValue;
	
	Turn(int turnValue) {
		this.turnValue = turnValue;
	}
}
