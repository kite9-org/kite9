package org.kite9.diagram.visualization.planarization.mgt.router;

import java.util.Set;

import org.kite9.diagram.common.elements.vertex.AbstractVertex;
import org.kite9.diagram.common.elements.vertex.SingleElementVertex;
import org.kite9.diagram.model.DiagramElement;



/**
 * This is used where a routing goes from one side of the planarization axis to the other.
 * 
 * @author robmoffat
 */
public class PlanarizationCrossingVertex extends AbstractVertex implements SingleElementVertex {

	DiagramElement und;
	
	public PlanarizationCrossingVertex(String name, DiagramElement und) {
		super(name);
		this.und = und;
	}

	public DiagramElement getOriginalUnderlying() {
		return und;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return und == de;
	}

	
	
}