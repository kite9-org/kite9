package org.kite9.diagram.model;

import org.kite9.diagram.model.position.End;
import org.kite9.diagram.model.style.LabelPlacement;

/**
 * DiagramElement to contain a label for an edge, container or diagram.
 * Labels take up space on the diagram, so they have to be processed in the *orthogonalization* phase.
 * however they don't have connections so they are excluded from the Planarization phase.
 */
public interface Label extends Rectangular {
		
	public boolean isConnectionLabel();
	
	/**
	 * If this is a connection label, returns the end of the connection that it is for.
	 */
	public End getEnd();
	
	public LabelPlacement getLabelPlacement();
}
