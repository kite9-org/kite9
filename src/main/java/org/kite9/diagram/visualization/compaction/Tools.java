package org.kite9.diagram.visualization.compaction;

import org.kite9.diagram.common.elements.ArtificialElement;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

public class Tools implements Logable {

	Kite9Log log = new Kite9Log(this);
	
	public static void checkSingleDimensionChange(Dimension2D p1, Dimension2D last) {
		if (last!=null) {
			if ((last.x()!=p1.x()) && (last.y()!=p1.y())) {
				throw new LogicException("Only one dimension should change at a time");
			}
		}
	}
	
	/**
	 * Returns the user element represented by this construction element.
	 */
	public static Object getUltimateElement(ArtificialElement e) {
		Object out = e.getOriginalUnderlying();
		
		if (out==null) {
			return e;
		}
		
		return out;
	}


	public String getPrefix() {
		return "C_TL";
	}

	public boolean isLoggingEnabled() {
		return true;
	}
}
