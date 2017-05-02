package org.kite9.diagram.visualization.orthogonalization;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

/**
 * Contains some utility methods that can be used by Orthogonalizers.
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractOrthogonalizer implements Orthogonalizer, Logable {
	
	protected Kite9Log log = new Kite9Log(this);

	/**
	 * Helper method to ensure that the dart ordering starts with the top-left
	 * corner dart. Call after createDartOrdering
	 */
	public void orderDartsFromCorner(
			Map<Vertex, List<DartImpl>> cornerDarts, Orthogonalization om) {
		for (Vertex v : cornerDarts.keySet()) {

			if (v.hasDimension()) {
				
				List<Dart> ld = om.getDartOrdering(v);
				log.send(log.go() ? null : "Dart order (before): "+v+" is "+ld);

				List<DartImpl> corners = cornerDarts.get(v);
				if ((corners!=null) && (corners.size()>0)) {
					// move first corner round to start
					DartImpl cd = corners.get(0);
					int idx = ld.indexOf(cd);
					Collections.rotate(ld, -idx);
					log.send(log.go() ? null : "Dart order (after) : "+v+" is "+ld);
				}
			}
		}
	}

	public String getPrefix() {
		return "ORTH";
	}

	public boolean isLoggingEnabled() {
		return false;
	}
}
