package org.kite9.diagram.model;

/**
 * DiagramElement to contain a label an edge, container or diagram.
 * Labels take up space on the diagram, so they have to be processed in the *orthogonalization* phase.
 * however they don't have connections so they are excluded from the Planarization phase.
 */
public interface Label extends Rectangular {
		
	public boolean isConnectionLabel();
}
