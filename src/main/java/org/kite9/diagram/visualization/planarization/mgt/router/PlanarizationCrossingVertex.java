package org.kite9.diagram.visualization.planarization.mgt.router;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.AbstractVertex;



/**
 * This is used where a connection goes from one side of the planarization axis to the other.
 * 
 * @author robmoffat
 */
public class PlanarizationCrossingVertex extends AbstractVertex {

	DiagramElement und;
	
	public PlanarizationCrossingVertex(String name, DiagramElement und) {
		super(name);
		this.und = und;
	}

	public DiagramElement getOriginalUnderlying() {
		return und;
	}
	
}