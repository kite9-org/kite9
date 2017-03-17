package org.kite9.diagram.adl;

/**
 * DiagramElement to contain a label an edge, container or diagram.
 * Labels take up space on the diagram, so they have to be positioned in the compaction phase,
 * however they don't have connections so they are excluded from the Planarization and Orthogonalization 
 * phases.
 */
public interface Label extends DiagramElement, Rectangular {
	
	public boolean hasContent();
		
	@Deprecated
	public String getText();		// this won't be here later
	
	public boolean isConnectionLabel();
}
