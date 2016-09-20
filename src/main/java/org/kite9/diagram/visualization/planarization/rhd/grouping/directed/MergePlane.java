package org.kite9.diagram.visualization.planarization.rhd.grouping.directed;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;

public enum MergePlane {

	X_FIRST_MERGE, Y_FIRST_MERGE, UNKNOWN;

	public boolean matches(MergePlane state) {
		if (this==X_FIRST_MERGE) {
			if (state == MergePlane.Y_FIRST_MERGE) {
				return false;
			}
		} else if (this==Y_FIRST_MERGE) {
			if (state == MergePlane.X_FIRST_MERGE) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean withDirection(Direction d) {
		switch (d) {
		case UP:
		case DOWN:
			return this==Y_FIRST_MERGE;
		case LEFT:
		case RIGHT:
			return this==X_FIRST_MERGE;
		}
		
		return false;
	}

}
