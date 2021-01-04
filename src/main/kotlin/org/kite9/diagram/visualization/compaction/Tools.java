package org.kite9.diagram.visualization.compaction;

import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;
import org.kite9.diagram.logging.LogicException;
import org.kite9.diagram.model.position.Dimension2D;

public class Tools implements Logable {

	Kite9Log log = new Kite9Log(this);
	
	public static void checkSingleDimensionChange(Dimension2D p1, Dimension2D last) {
		if (last!=null) {
			if ((last.x()!=p1.x()) && (last.y()!=p1.y())) {
				throw new LogicException("Only one dimension should change at a time");
			}
		}
	}

	public String getPrefix() {
		return "C_TL";
	}

	public boolean isLoggingEnabled() {
		return true;
	}
}
