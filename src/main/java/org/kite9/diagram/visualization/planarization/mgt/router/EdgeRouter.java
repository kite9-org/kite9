package org.kite9.diagram.visualization.planarization.mgt.router;

import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;

/**
 * Handles inserting of edges into the GT planarization.
 * 
 * @author robmoffat
 *
 */
public interface EdgeRouter {

	/**
	 * Called for each edge added to the planarization.  Edges must be added in aboveSet, belowSet
	 * onLine, or a combination of the above.  Otherwise, false is returned.
	 */
	public boolean addPlanarizationEdge(MGTPlanarization p, PlanarizationEdge edge, Direction d, CrossingType it, GeographyType gt);
	
}
