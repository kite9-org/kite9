package org.kite9.diagram.common.algorithms.so;

import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.position.SegmentSlackOptimisation;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

/**
 * Ensures that the overall diagram width is minimal.
 * @author robmoffat
 *
 */
public class WidthOptimisationStep implements OptimisationStep, Logable {

	Kite9Log log = new Kite9Log(this);
	
	public void optimise(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo) {
		xo.updateCanonicalOrdering();
		Slideable lastx = xo.getCanonicalOrder().get(xo.getCanonicalOrder().size()-1);
		// Slideable firstx = xo.getCanonicalOrder().get(0);
		lastx.decreaseMaximum(lastx.getMinimumPosition());
		
//		xo.ensureMaximumDistance(firstx, lastx, lastx.getMinimumPosition(), true);
//		log.send(log.go() ? null : "X Axis Pushes: "+xo.getPushCount());

		yo.updateCanonicalOrdering();
		Slideable lasty = yo.getCanonicalOrder().get(yo.getCanonicalOrder().size()-1);
		//Slideable firsty = yo.getCanonicalOrder().get(0);
		lasty.decreaseMaximum(lasty.getMinimumPosition());
		
//		yo.ensureMaximumDistance(firsty, lasty, lasty.getMinimumPosition(), true);
//		log.send(log.go() ? null : "Y Axis Pushes: "+yo.getPushCount());
	}

	public String getPrefix() {
		return "WOS ";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
